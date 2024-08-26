package popz.solpop.controller;


import popz.solpop.dto.*;
import popz.solpop.security.TokenProvider;
import popz.solpop.repository.MemberRepository;
import popz.solpop.service.AuthService;
import popz.solpop.entity.Member;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import popz.solpop.service.MailService;
import popz.solpop.service.MemberService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    AuthService authService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MailService mailService;

    @GetMapping("/check-id")
    public Response<?> checkId(@RequestParam String userName) {
        boolean exists = memberRepository.existsByUserName(userName);
        if (exists) {
            return Response.setFailed("중복되는 아이디가 존재합니다.");
        } else {
            return Response.setSuccess("사용 가능한 아이디입니다.");
        }
    }

    @PostMapping("/signUp")
    public Response<?> signUp(@RequestBody SignUp requestBody) {
        Response<?> result = authService.signUp(requestBody);
        return result;
    }

    @PostMapping("/login")
    public Response<?> login(@RequestBody Login requestBody, HttpServletResponse response) {
        Response<?> result = authService.login(requestBody, response);
        return result;
    }

    @PostMapping("/logout")
    public Response<?> logout(HttpServletResponse response) {
        // Refresh Token 쿠키 제거
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS를 사용하는 경우에만 적용
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(refreshTokenCookie);
        return Response.setSuccess("로그아웃에 성공했습니다.");
    }

    // 유저 계좌, 유저 키, 이메일, 이름 포함 전달하기
    @GetMapping("/check-token")
    public Response<?> checkToken(@RequestHeader("Authorization") String token) {
        token = token.substring(7);

        Map<String, Object> tokenData = tokenProvider.validateJwt(token);
        if (tokenData == null) {
            return Response.setFailed("엑세스토큰이 유효하지 않습니다.");
        }

        Date issuedAt = (Date) tokenData.get("issuedAt");
        long elapsedSeconds = (new Date().getTime() - issuedAt.getTime()) / 1000;
        int accessTokenDuration = 3600;

        if (elapsedSeconds > accessTokenDuration) {
            return Response.setFailed("엑세스토큰이 만료되었습니다.");
        }

        return Response.setSuccess("엑세스토큰이 유효합니다. 경과 시간: " + elapsedSeconds + "초");
    }

    @PostMapping("/refresh-token")
    public Response<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return Response.setFailed("리프레시 토큰이 존재하지 않습니다.");
        }

        Map<String, Object> tokenData = tokenProvider.validateJwt(refreshToken);
        if (tokenData == null) {
            return Response.setFailed("리프레시 토큰이 유효하지 않습니다.");
        }

        String userName = (String) tokenData.get("userName");

        Member memberEntity = memberRepository.findMemberByUserName(userName);
        if (memberEntity == null || !memberEntity.getToken().equals(refreshToken)) {
            return Response.setFailed("리프레시 토큰이 유효하지 않습니다.");
        }

        int accessTokenDuration = 3600;
        String newAccessToken = tokenProvider.createAccessToken(userName, accessTokenDuration);

        if (newAccessToken == null) {
            return Response.setFailed("엑세스토큰 생성에 실패했습니다.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);

        return Response.setSuccessData("엑세스토큰 재발급에 성공하였습니다.", data);
    }

    @GetMapping("/check/findPassword")
    public Boolean findPassword(
            @RequestBody FindPwRequest findPwRequest){
        return memberService.userEmailCheck(findPwRequest.getUserId(), findPwRequest.getName());
    }


    @PostMapping("/check/findPassword/sendEmail")
    public void sendEmail(
            @RequestBody FindPwRequest findPwRequest
    ){
        FindPwResponse findPwResponse = mailService.createMailAndChangePassword(findPwRequest.getUserId(), findPwRequest.getName());
        mailService.mailSend(findPwResponse);
    }

    @PostMapping("/webClient")
    public SSAFYUserResponse postWebClient(
            @RequestBody SSAFYUserRequest ssafyUserRequest)  {
        return authService.createSSAFYUser(ssafyUserRequest);
    }

}


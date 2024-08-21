package popz.solpop.repository;


import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import popz.solpop.entity.Like;

@Repository
@Transactional
public interface LikeRepository extends JpaRepository<Like, Integer> {


}

package popz.solpop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import popz.solpop.repository.RaffleRepository;


@Service
@Transactional
public class RaffleService {

  @Autowired
  private RaffleRepository raffleRepository;
}
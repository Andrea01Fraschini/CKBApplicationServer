package BersaniChiappiniFraschini.AuthenticationService.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface PairKeyValueRepository extends JpaRepository<PairKeyValue, String> {
    Optional<PairKeyValue> findPairKeyValueByKey(String key);
}

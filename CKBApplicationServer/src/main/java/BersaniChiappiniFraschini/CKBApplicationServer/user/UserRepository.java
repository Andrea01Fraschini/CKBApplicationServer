package BersaniChiappiniFraschini.CKBApplicationServer.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

//No need for @Repository, already inherited from MongoRepository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByEmail(String email); //Magically implemented by spring
    Optional<User> findUserByUsername(String username);
}

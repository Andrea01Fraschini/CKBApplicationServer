package BersaniChiappiniFraschini.CKBApplicationServer.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Collection;
import java.util.Optional;

//No need for @Repository, already inherited from MongoRepository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByEmail(String email); //Magically implemented by spring
    Optional<User> findUserByUsername(String username);
    boolean existsUserByEmail(String email);
    boolean existsUserByUsername(String email);
    @Query(" { 'username': {$regex : ?1, $options: 'i'}, 'accountType': ?0 } ")
    Collection<User> findByAccountTypeAndUsernameLike(AccountType accountType, String name);
}

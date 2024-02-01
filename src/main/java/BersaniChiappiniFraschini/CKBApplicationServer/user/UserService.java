package BersaniChiappiniFraschini.CKBApplicationServer.user;

import BersaniChiappiniFraschini.CKBApplicationServer.invite.Invite;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that allows to modify and search user information
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final MongoTemplate mongoTemplate;



    public List<String> searchEducatorByName(String name){
        var users =  userRepository.findByAccountTypeAndUsernameLike(AccountType.EDUCATOR, name);
        return users.stream().map(User::getUsername).toList();
    }

    public List<String> searchStudentByName(String name) {
        var users =  userRepository.findByAccountTypeAndUsernameLike(AccountType.STUDENT, name);
        return users.stream().map(User::getUsername).toList();
    }

    public List<UserController.UsernameAndType> searchUserByName(String name) {
        var users =  userRepository.findUsersByUsernameLike(name);

        return users.stream().map(u ->
                new UserController.UsernameAndType(
                        u.getUsername(),
                        u.getAccountType().name()))
                .toList();
    }

    public ResponseEntity<UserController.UsernameAndType> getUserData() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (User) userDetailsService.loadUserByUsername(auth.getName());
        return ResponseEntity.ok(new UserController.UsernameAndType(
                user.getUsername(),
                user.getAccountType().name()
        ));
    }

    public void addNotification(String email, Notification notification){
        var update = new Update();
        update.push("notifications", notification);
        var criteria = Criteria.where("email").in(email);
        mongoTemplate.updateFirst(Query.query(criteria), update, "user");
    }

    public void addInvite(Invite invite){
        var update = new Update();
        update.push("invites", invite);
        var criteria = Criteria.where("username").is(invite.getReceiver());
        mongoTemplate.updateFirst(Query.query(criteria), update, "user");
    }
}

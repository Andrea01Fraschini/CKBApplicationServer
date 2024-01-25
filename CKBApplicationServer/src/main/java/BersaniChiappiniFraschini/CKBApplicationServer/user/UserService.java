package BersaniChiappiniFraschini.CKBApplicationServer.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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
}

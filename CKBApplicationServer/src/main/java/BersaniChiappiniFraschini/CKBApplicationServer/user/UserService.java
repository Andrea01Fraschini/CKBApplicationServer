package BersaniChiappiniFraschini.CKBApplicationServer.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<String> searchEducatorByName(String username){
        //username = username.toLowerCase().trim();
        System.out.println(username);
        var users =  userRepository.findByAccountTypeAndUsernameLike(AccountType.EDUCATOR, username);
        System.out.println(users.size());
        return users.stream().map(User::getUsername).toList();
    }
}

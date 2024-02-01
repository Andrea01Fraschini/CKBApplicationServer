package BersaniChiappiniFraschini.CKBApplicationServer.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/educator")
    @ResponseBody
    public List<String> searchEducatorByName(
            @RequestParam(value = "name") String name
    ){
        return userService.searchEducatorByName(name);
    }

    @GetMapping("/student")
    @ResponseBody
    public List<String> searchStudentByName(
            @RequestParam(value = "name") String name
    ){
        return userService.searchStudentByName(name);
    }

    @GetMapping("/user")
    @ResponseBody
    public List<UsernameAndType> searchUserByName(
            @RequestParam(value = "name") String name
    ){
        return userService.searchUserByName(name);
    }

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<UsernameAndType> getUserDataB() {
        return userService.getUserData();
    }

    public record UsernameAndType(
      String username,
      String account_type
    ){}
}

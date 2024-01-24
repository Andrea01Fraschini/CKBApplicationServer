package BersaniChiappiniFraschini.CKBApplicationServer.user;

import lombok.RequiredArgsConstructor;
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
}

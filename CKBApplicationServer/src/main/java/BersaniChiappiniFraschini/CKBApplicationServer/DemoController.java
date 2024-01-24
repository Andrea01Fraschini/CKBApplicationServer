package BersaniChiappiniFraschini.CKBApplicationServer;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> test(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        return ResponseEntity.ok("Hello %s".formatted(name));
    }
}

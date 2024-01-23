package BersaniChiappiniFraschini.CKBApplicationServer;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("Hello authenticated user!");
    }
}

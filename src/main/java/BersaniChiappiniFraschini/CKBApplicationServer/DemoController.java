package BersaniChiappiniFraschini.CKBApplicationServer;

import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {
    private final NotificationService service;
    @GetMapping
    public ResponseEntity<String> test(
            //@RequestBody TournamentCreationRequest request
            ){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return ResponseEntity.ok("Hello %s".formatted(name));
    }

    @PostMapping
    public ResponseEntity<String> test2(
            //@RequestBody TournamentCreationRequest request
    ){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        String message = "Hello %s".formatted(name);
        System.out.println(message);
        return ResponseEntity.ok(message);
    }
}

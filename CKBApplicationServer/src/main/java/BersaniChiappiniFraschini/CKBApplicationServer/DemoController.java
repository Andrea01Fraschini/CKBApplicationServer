package BersaniChiappiniFraschini.CKBApplicationServer;

import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentCreationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> test(
            @RequestBody TournamentCreationRequest request
            ){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        return ResponseEntity.ok("Hello %s".formatted(name) + request.toString());
    }

    @PostMapping
    public ResponseEntity<String> test2(
            @RequestBody TournamentCreationRequest request
    ){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        return ResponseEntity.ok("Hello %s".formatted(name) + request.toString());
    }
}

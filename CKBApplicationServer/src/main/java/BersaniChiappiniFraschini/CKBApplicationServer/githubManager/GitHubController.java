package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import BersaniChiappiniFraschini.CKBApplicationServer.authentication.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GitHubController {
    private final GitHubManagerService gitHubManagerService;
    @GetMapping
    public String register(
            @RequestParam String tournamentTitle,
            @RequestParam String battleTitle
    ){
        return gitHubManagerService.createRepository(tournamentTitle, battleTitle);
    }
}

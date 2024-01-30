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

    // for test
    @GetMapping("/create")
    public String register(
            @RequestParam String tournamentTitle,
            @RequestParam String battleTitle,
            @RequestParam String description
    ){
        return gitHubManagerService.createRepository(tournamentTitle, battleTitle, description);
    }

    // for test
    @GetMapping("/upload")
    public boolean createCode(
            @RequestParam String repository,
            @RequestParam String pathFile
    ){
        return gitHubManagerService.setCodeRepository(repository, pathFile);
    }
}

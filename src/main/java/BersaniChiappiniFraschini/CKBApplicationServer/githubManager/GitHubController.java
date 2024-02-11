package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubManagerService gitHubManagerService;
    private final PushActionService pushActionService;

    @PostMapping("/push")
    public ResponseEntity<String> pushActionPerformed(
            @RequestHeader(name = "Authorization") String authorization
    ){
        return pushActionService.fetchAndTestCode(authorization);
    }

    @GetMapping("/download")
    public String downloadCode(
            @RequestParam String repository,
            @RequestParam String path
    ){
        return gitHubManagerService.downloadRepo(repository, path);
    }

}

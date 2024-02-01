package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/repository")
    public ResponseEntity<PostResponse> setRepository(
            @RequestBody GroupRequest groupRequest
    ) {
        return groupService.setRepository(groupRequest);
    }
}

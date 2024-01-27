package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invites")
public class InviteController {
    private final InviteService inviteService;

    @PostMapping("/group")
    public ResponseEntity<PostResponse> inviteToGroup(
            @RequestBody GroupInviteRequest request
    ) {
        return inviteService.sendGroupInvite(request);
    }

    @PostMapping("/tournament")
    public ResponseEntity<PostResponse> inviteToTournament() {
        return inviteService.sendManagerInvite(null, null, null);
    }

    @PostMapping("/group/update")
    public ResponseEntity<PostResponse> updateGroupInviteStatus(
            @RequestBody InviteStatusUpdateRequest request
    ) {
        return inviteService.updateGroupInviteStatus(request); 
    }
}

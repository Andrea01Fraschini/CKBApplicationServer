package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PostResponse> inviteToTournament(
            @RequestBody ManagerInviteRequest request
    ) {
        return inviteService.sendManagerInvite(request);
    }

    @PostMapping("/update")
    public ResponseEntity<PostResponse> updateGroupInviteStatus(
            @RequestBody InviteStatusUpdateRequest request
    ) {
        return inviteService.updateInviteStatus(request);
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<InviteCard>> getUserInvites() {
        return inviteService.getUserInviteCards();
    }

    public record InviteCard(
        String invite_id,
        String sender,
        String tournament_id,
        String tournament_title,
        String battle_title // not present in educators' requests
        // TODO: optional, users list
        // List<String> users // educators or students depending on request (on hold)
    ) {}
}

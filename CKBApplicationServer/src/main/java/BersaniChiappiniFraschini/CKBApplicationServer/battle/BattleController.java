package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/battles")
public class BattleController {
    private final BattleService battleService;
    private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createBattle(
            @RequestBody BattleCreationRequest request
    ) {
        return battleService.createBattle(request);
    }

    @PostMapping("/enroll")
    public ResponseEntity<PostResponse> enrollGroup(
            @RequestBody BattleEnrollmentRequest request
    ) {
        return battleService.enrollGroup(request);
    }

    // Work in Progress
    @PostMapping("/push-action")
    public ResponseEntity<PostResponse> pushAction(
            @RequestBody String request // TODO: replace
    ) {
        return eventService.handlePushEvent(request);
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/battles")
public class BattleController {
    private final BattleService battleService;
    // private final EventService eventService;

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

    //here the view of the battle in detail
    @GetMapping("/view")
    public ResponseEntity<Object> getBattle(
            @RequestParam String tournamentTitle,
            @RequestParam String battleTitle
    ) {
        return battleService.getBattle(tournamentTitle, battleTitle);
    }
}

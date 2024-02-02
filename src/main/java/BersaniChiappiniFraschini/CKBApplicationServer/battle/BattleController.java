package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequiredArgsConstructor
@RequestMapping("/battles")
public class BattleController {
    private final BattleService battleService;
    // private final EventService eventService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createBattle(
            @RequestPart("request") BattleCreationRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        if (file.getContentType() != null && (file.getContentType().equals("application/zip") || file.getContentType().equals("application/x-zip-compressed"))){
            request.setFile(file);
            return battleService.createBattle(request);
        } else {
            PostResponse pos = new PostResponse("The file is not a zip");
            return ResponseEntity.badRequest().body(pos);
        }
    }

    @PostMapping("/enroll")
    public ResponseEntity<PostResponse> enrollGroup(
            @RequestBody BattleEnrollmentRequest request
    ) {
        return battleService.enrollGroup(request);
    }

    // here the view of the battle in detail
    @GetMapping("/view")
    public ResponseEntity<Object> getBattle(
            @RequestParam String tournamentTitle,
            @RequestParam String battleTitle
    ) {
        return battleService.getBattleInfo(tournamentTitle, battleTitle);
    }

}

package BersaniChiappiniFraschini.CKBApplicationServer.scores;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManualEvaluationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping("/manual_assessment")
    public ResponseEntity<PostResponse> setManualPoints(
            @RequestBody ManualEvaluationRequest manualEvaluationUpdate
    ){
        return scoreService.setManualScores(manualEvaluationUpdate);
    }
}

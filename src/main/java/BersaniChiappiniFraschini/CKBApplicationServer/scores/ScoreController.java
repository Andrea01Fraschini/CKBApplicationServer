package BersaniChiappiniFraschini.CKBApplicationServer.scores;


import BersaniChiappiniFraschini.CKBApplicationServer.battle.BattleService;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManualEvaluationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping("/manualscores")
    public ResponseEntity<PostResponse> setManualPoints(
            @RequestBody ManualEvaluationRequest manualEvaluationUpdate
    ){
        return scoreService.setManualScores(manualEvaluationUpdate);
    }
}

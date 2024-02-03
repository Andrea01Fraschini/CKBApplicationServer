package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.analysis.CodeAnalysisService;
import BersaniChiappiniFraschini.CKBApplicationServer.analysis.EvaluationResult;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.BattleService;
import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PushActionService {
    private final JwtService jwtService;
    private final CodeAnalysisService codeAnalysisService;
    private final GitHubManagerService gitHubManagerService;
    private final BattleService battleService;
    private final ScoreService scoreService;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public ResponseEntity<String> fetchAndTestCode(String authorization) {
        if(authorization.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'Authorization' in the header");
        }

        String token = authorization.substring(7);
        String group_id = jwtService.extractUsername(token);

        Battle battle = battleService.getBattleFromGroupId(group_id);

        if(jwtService.isExpired(token)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Expired API token");
        }

        if(battle == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Group not found");
        }

        var foundGroups = battle.getGroups().stream()
                .filter(g -> g.getId().equals(group_id))
                .toList();

        if(foundGroups.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
        }

        Group group = foundGroups.get(0);
        String repository = group.getRepository();

        if(repository.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No repository set for this group");
        }

        Runnable task = () -> fetchTestAndUpdate(battle, group);
        new Thread(task).start();

        return ResponseEntity.ok("ok");
    }

    private void fetchTestAndUpdate(Battle battle, Group group){
        Date now = new Date(System.currentTimeMillis());

        if(now.after(battle.getSubmission_deadline()) || now.before(battle.getEnrollment_deadline())){
            return;
        }


        // =============== FETCH ===============
        int index = counter.getAndIncrement();
        String dirName = "./repos_%d".formatted(index);

        gitHubManagerService.downloadRepo(group.getRepository(), dirName+"/");

        // =============== TEST ===============
        EvaluationResult results;
        try {
            results = codeAnalysisService.launchAutomatedAssessment(dirName, battle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Clean
            deleteDirectory(new File(dirName));
        }
        System.out.println(results);

        // =============== UPDATE SCORE AND INFO ===============
        scoreService.updateGroupAfterAutomaticEvaluation(group.getId(), results);
    }


    private static void deleteDirectory(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        file.delete();
    }
}

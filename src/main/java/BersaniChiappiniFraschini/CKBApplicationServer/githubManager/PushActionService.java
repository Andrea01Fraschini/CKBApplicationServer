package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.analysis.CodeAnalysisService;
import BersaniChiappiniFraschini.CKBApplicationServer.analysis.EvaluationResult;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.BattleService;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PushActionService {
    private final JwtService jwtService;
    private final BattleService battleService;
    private final CodeAnalysisService codeAnalysisService;
    private final GitHubManagerService gitHubManagerService;

    public ResponseEntity<String> fetchAndTestCode(String authorization) {
        if(authorization.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'Authorization' in the header");
        }

        String token = authorization.substring(7);
        String group_id = jwtService.extractUsername(token);

        Battle battle = battleService.getBattleFromGroupId(group_id);

        if(battle == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
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
        task.run();

        return ResponseEntity.ok("Push correctly received!");
    }

    private void fetchTestAndUpdate(Battle battle, Group group){

        // Fetch
        gitHubManagerService.downloadRepo(group.getRepository(), "./repos/");
        String testFileName = battle.getTests_file_name();
        String language = battle.getProject_language();

        try{
            try(var file = new FileInputStream("./repos/message.txt")){
                String filecontents = new String(file.readAllBytes());
                System.out.println(filecontents+"\n"+"Test file name: %s\nLanguage: %s".formatted(testFileName, language));
            }
        }catch (Exception ignored){}

        /*

        // Test
        var evaluationParams = List.of(
                EvalParameter.QUALITY,
                EvalParameter.RELIABILITY,
                EvalParameter.SECURITY
        );

        EvaluationResult results;

        try {
            results = codeAnalysisService.launchAutomatedAssessment(
                    "./repos/",
                    testFileName,
                    evaluationParams,
                    language);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
*/

        // Clean
        assert deleteDirectory(new File("./repos"));

        // Update

    }

    private static boolean deleteDirectory(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        return file.delete();
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.scores;

import BersaniChiappiniFraschini.CKBApplicationServer.analysis.EvaluationResult;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManualEvaluationRequest;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.TestStatus;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentSubscriber;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final UserDetailsService userDetailsService;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // set manual points
    public ResponseEntity<PostResponse> setManualScores(ManualEvaluationRequest manualEvaluationUpdate){
        var tournament_title = manualEvaluationUpdate.getTournament_title();
        var battle_title = manualEvaluationUpdate.getBattle_title();
        var group_id = manualEvaluationUpdate.getGroup_id();
        var manualAssessmentScore = manualEvaluationUpdate.getPoints();

        var auth = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is EDUCATOR
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new PostResponse("Cannot add manual evaluation as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var educator = (User) userDetailsService.loadUserByUsername(auth.getName());

        Query query = new Query(Criteria
                .where("title").is(tournament_title)
                .and("educators._id").is(new ObjectId(educator.getId()))
                .and("battles.manual_evaluation").is(true)
                .and("battles.groups._id").is(new ObjectId(group_id))
                .and("battles.groups.done_manual_evaluation").is(false));

        var update = new Update()
                .set("battles.$.groups.$[group].evaluation_result.manual_assessment_score", manualAssessmentScore)
                .set("battles.$.groups.$[group].done_manual_evaluation",true)
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        // Update evaluation results with manual assessment score and set done_manual_evaluation to true
        mongoTemplate.updateFirst(query, update, "tournament");

        // Recompute total scores and ranks
        updateGroupTotalScore(group_id);

        // Send notifications for manual evaluation
        notifyRankIfNoMissingManualEvaluation(tournament_title, battle_title);

        PostResponse p = new PostResponse("OK");
        return ResponseEntity.ok().body(p);
    }


    public void updateGroupTotalScore(String group_id){
        Query query = new Query(Criteria
                .where("battles.groups._id").is(new ObjectId(group_id)));

        Tournament tournament = mongoTemplate.findOne(query, Tournament.class, "tournament");

        if(tournament == null) return;

        Group group = null;

        // Find group in tournament (ugly, I know)
        for(var battle : tournament.getBattles()){
            var searchResult = battle.getGroups().stream().filter(g->g.getId().equals(group_id)).toList();
            if(!searchResult.isEmpty()){
                group = searchResult.get(0);
                break;
            }
        }

        if (group == null){
            throw new RuntimeException("Group not found in any battle but should be present in tournament");
        }

        var totalScore = computeTotalScore(group.getEvaluation_result());

        var update = new Update()
                .set("battles.$.groups.$[group].total_score", totalScore)
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    // This is probably the worst method in the entire codebase.
    public void updatePlayersRanks(String group_id){
        // TODO: fix duplicated code by changing closeBattle in BattleService
        Query query = new Query(Criteria
                .where("battles.groups._id").is(new ObjectId(group_id)));

        Tournament tournament = mongoTemplate.findOne(query, Tournament.class, "tournament");

        if(tournament == null) return;

        Group group = null;

        for(var battle : tournament.getBattles()){
            var searchResult = battle.getGroups().stream().filter(g->g.getId().equals(group_id)).toList();
            if(!searchResult.isEmpty()){
                group = searchResult.get(0);
                break;
            }
        }

        if (group == null){
            throw new RuntimeException("Group not found in any battle but should be present in tournament");
        }

        var tournamentSubscribers = tournament.getSubscribed_users();
        for(GroupMember m : group.getMembers()){
            for(TournamentSubscriber ts : tournamentSubscribers){
                if(ts.getUsername().equals(m.getUsername())){
                    int finalScore = ts.getScore() + group.getTotal_score();
                    ts.setScore(finalScore);
                }
            }
        }

        Query query2 = new Query(Criteria
                .where("_id").is(new ObjectId(tournament.getId())));

        var update2 = new Update()
                .set("subscribed_users", tournamentSubscribers);

        mongoTemplate.updateFirst(query2, update2, "tournament");
    }

    private void notifyRankIfNoMissingManualEvaluation(String tournament_title, String battle_title){
        Query query = new Query(Criteria
                .where("title").is(tournament_title)
                .and("battles.title").is(battle_title));

        Tournament tournament = mongoTemplate.findOne(query, Tournament.class,"tournament");

        if(tournament == null) return;

        Battle battle = tournament.getBattles()
                .stream().filter((b) -> b.getTitle().equals(battle_title)).toList().get(0);

        for(Group g : battle.getGroups()){
            if(!g.isDone_manual_evaluation()) return;
        }

        for (var group : battle.getGroups()) {
            Runnable taskSendEmail = () -> notificationService.sendNewBattleRankAvailable(group, tournament.getTitle(), battle.getTitle());
            executor.submit(taskSendEmail);
        }
    }

    public void updateGroupAfterAutomaticEvaluation(String groupId, EvaluationResult results){
        Integer new_score = computeTotalScore(results);

        Date now = new Date(System.currentTimeMillis());

        Query query = new Query(Criteria
                .where("battles.groups._id").is(new ObjectId(groupId)));

        var update = new Update()
                .set("battles.$.groups.$[group].total_score", new_score)
                .set("battles.$.groups.$[group].evaluation_result", results)
                .set("battles.$.groups.$[group].last_update", now)
                .filterArray(Criteria.where("group._id").is(new ObjectId(groupId)));

        mongoTemplate.updateFirst(query, update, "tournament");

        updateGroupTotalScore(groupId);
    }

    private Integer computeTotalScore(EvaluationResult results) {
        var tests = results.getTests_results();
        var staticAnalysis = results.getStatic_analysis_results();
        var timeliness = results.getTimeliness_score();
        Integer manualAssessmentScore = results.getManual_assessment_score();

        // All these scores/results are calculated simultaneously,
        // so here || and && are equivalent since they are either all null or all set
        if(tests == null || staticAnalysis == null || timeliness == null){
            return Objects.requireNonNullElse(manualAssessmentScore, 0);
        }

        long numPassedTests = tests.values().stream().filter(v->v.equals(TestStatus.PASSED)).count();
        long numTests = tests.keySet().size();

        float testScore = (float) numPassedTests /numTests * 100;

        float staticScore = (float) staticAnalysis.values().stream().reduce(Integer::sum).orElse(0)
                / staticAnalysis.keySet().size();

        int automaticScore = (int) (0.3 * staticScore + 0.5 * testScore + 0.2 * timeliness);

        // manualAssessmentScore can be null
        int manualScore = Objects.requireNonNullElse(manualAssessmentScore, automaticScore);

        double finalScore = 0.7 * manualScore + 0.3 * automaticScore;

        return (int) finalScore;
    }
}

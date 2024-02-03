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
        var tournament_id = manualEvaluationUpdate.getTournament_id();
        var battle_id = manualEvaluationUpdate.getBattle_id();
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
                .where("_id").is(new ObjectId(tournament_id))
                .and("educators._id").is(new ObjectId(educator.getId())) // Update iff educator is a manager
                .and("battles._id").is(new ObjectId(battle_id))
                .and("battles.manual_evaluation").is(true)
                .and("battles.groups._id").is(new ObjectId())
                .and("battles.groups.done_manual_evaluation").is(false));

        var update = new Update()
                .set("battles.$.groups.$[group].evaluation_result.manual_assessment_score", manualAssessmentScore)
                .set("battles.$.groups.$[group].done_manual_evaluation",true)
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        // Update evaluation results with manual assessment score and set done_manual_evaluation to true
        mongoTemplate.updateFirst(query, update, "tournament");

        // Recompute total scores and ranks
        updateTotalScoreAndRanks(group_id);

        // Send notifications for manual evaluation
        notifyRankIfNoMissingManualEvaluation(tournament_id, battle_id);

        PostResponse p = new PostResponse("OK");
        return ResponseEntity.ok().body(p);
    }

    public void updateTotalScoreAndRanks(String group_id){
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

        // update also the personal score rank in the tournament
        List<TournamentSubscriber> tournamentSubscriber = tournament.getSubscribed_users();

        for(GroupMember m : group.getMembers()){
            for(TournamentSubscriber ts : tournamentSubscriber){
                if(ts.getUsername().equals(m.getUsername())){
                    int finalScore = ts.getScore() + totalScore;
                    ts.setScore(finalScore);
                }
            }
        }

        Query query2 = new Query(Criteria
                .where("_id").is(new ObjectId(tournament.getId())));

        var update2 = new Update()
                .set("subscribed_users", tournamentSubscriber);

        mongoTemplate.updateFirst(query2, update2, "tournament");
    }

    private void notifyRankIfNoMissingManualEvaluation(String tournament_id, String battle_id){
        Query query = new Query(Criteria
                .where("_id").is(new ObjectId(tournament_id))
                .and("battles._id").is(new ObjectId(battle_id)));

        Tournament tournament = mongoTemplate.findOne(query, Tournament.class,"tournament");

        if(tournament == null) return;

        Battle battle = tournament.getBattles()
                .stream().filter((b) -> b.getId().equals(battle_id)).toList().get(0);

        for(Group g : battle.getGroups()){
            if(!g.isDone_manual_evaluation()) return;
        }

        for (var group : battle.getGroups()) {
            Runnable taskSendEmail = () -> notificationService.sendNewBattleRankAvailable(group, tournament.getTitle(), battle.getTitle());
            executor.submit(taskSendEmail);
        }

        return;
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

        updateTotalScoreAndRanks(groupId);
    }

    private Integer computeTotalScore(EvaluationResult results) {
        var tests = results.getTests_results();
        var staticAnalysis = results.getStatic_analysis_results();
        var timeliness = results.getTimeliness_score();
        Integer manualAssessmentScore = results.getManual_assessment_score();

        // all tests must pass
        for(var test : tests.values()){
            if(test.equals(TestStatus.FAILED)){
                return 0;
            }
        }

        float staticScore = 0.0f;
        for(var param : staticAnalysis.keySet()){
            staticScore += staticAnalysis.get(param);
        }
        staticScore = staticScore/staticAnalysis.keySet().size();

        int automaticScore = (int) (0.6 * staticScore + 0.4 * timeliness);

        // manualAssessmentScore can be null
        int manualScore = Objects.requireNonNullElse(manualAssessmentScore, automaticScore);

        double finalScore = 0.7 * manualScore + 0.3 * automaticScore;

        return (int) finalScore;
    }
}

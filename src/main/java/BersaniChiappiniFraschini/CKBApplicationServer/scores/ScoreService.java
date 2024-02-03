package BersaniChiappiniFraschini.CKBApplicationServer.scores;

import BersaniChiappiniFraschini.CKBApplicationServer.analysis.EvaluationResult;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManualEvaluationRequest;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
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
    public ResponseEntity<PostResponse> setManualScores(ManualEvaluationRequest groupRequest){

        // check is an educator and has the permission for the manual points
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();

        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new PostResponse("Cannot add manual evaluation as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var educator = (User) userDetailsService.loadUserByUsername(auth.getName());

        Query query = new Query(Criteria
                .where("_id").is(new ObjectId(groupRequest.getTournament_id()))
                .and("educators._id").is(new ObjectId(educator.getId()))
                .and("battles.manual_evaluation").is(true)
                .and("battles.groups._id").is(new ObjectId(groupRequest.getGroup_id()))
                .and("battles.groups.done_manual_evaluation").is(false));

        var update = new Update()
                .set("battles.$.groups.$[group].scores."+ EvalParameter.MANUAL.name(), groupRequest.getPoints())
                .set("battles.$.groups.$[group].done_manual_evaluation",true)
                .filterArray(Criteria.where("group._id").is(new ObjectId(groupRequest.getGroup_id())));

        mongoTemplate.updateFirst(query, update, "tournament");

        // UPDATE THE PERSONAL SCORES AND BATTLE RANK
        consolidateScores(groupRequest.getTournament_id(), groupRequest.getBattle_id(), groupRequest.getGroup_id());

        // SEND EMAIL OF THE FINAL BATTLE RANK IF AND ONLY IF ALL MANUAL EVALUATIONS ARE DONE
        notifyRankIfNoMissingManualEvaluation(groupRequest.getTournament_id(), groupRequest.getBattle_id());

        PostResponse p = new PostResponse("OK");
        return ResponseEntity.ok().body(p);
    }


    // FINAL BATTLE RANK AVAILABLE => must be called every time the scores of the battle is changed
    // ALSO UPDATE THE PERSONAL RANK OF THE TOURNAMENT
    public void consolidateScores(String tournament_id, String battle_id, String group_id){
        Query query = new Query(Criteria
                .where("_id").is(new ObjectId(tournament_id))
                .and("battles._id").is(new ObjectId(battle_id))
                .and("battles.groups._id").is(new ObjectId(group_id)));

        int totalScore;

        Tournament result = mongoTemplate.findOne(query, Tournament.class, "tournament");

        if(result == null) return;

        Group group = result.getBattles()
                .stream().filter((b) -> b.getId().equals(battle_id)).toList().get(0)
                .getGroups()
                .stream().filter((g) -> g.getId().equals(group_id)).toList().get(0);

        totalScore = group.getTotal_score();

        var update = new Update()
                .set("battles.$.groups.$[group].total_score", totalScore)
                .set("battles.$.groups.$[group].done_manual_evaluation",true)
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");

        // update also the personal score rank in the tournament
        List<TournamentSubscriber> tournamentSubscriber = result.getSubscribed_users();

        for(GroupMember m : group.getMembers()){
            for(TournamentSubscriber ts : tournamentSubscriber){
                if(ts.getUsername().equals(m.getUsername())){
                    int finalScore = ts.getScore() + totalScore;
                    ts.setScore(finalScore);
                }
            }
        }

        Query query2 = new Query(Criteria
                .where("_id").is(new ObjectId(tournament_id)));

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

    public void updateGroupAfterEvaluation(String groupId, Integer new_score, EvaluationResult results){
        Date now = new Date(System.currentTimeMillis());

        Query query = new Query(Criteria
                .where("battles.groups._id").is(new ObjectId(groupId)));

        var update = new Update()
                .set("battles.$.groups.$[group].total_score", new_score)
                .set("battles.$.groups.$[group].evaluation_result", results)
                .set("battles.$.groups.$[group].last_update", now)
                .filterArray(Criteria.where("group._id").is(new ObjectId(groupId)));

        mongoTemplate.updateFirst(query, update, "tournament");

    }
}

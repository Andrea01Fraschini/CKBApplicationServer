package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that gathers information to construct the dashboard view for the user
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TournamentRepository tournamentRepository;

    private final MongoTemplate mongoTemplate;
    public DashboardResponse getDashboard() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        String username = auth.getName();
        //Prendi le notifiche

        List<CardInfo> cards = new ArrayList<>();
        switch (accountType) {
            case STUDENT -> {

                //tournament_title: from title of tournament from TOURNAMENT
                //battle_title: from title of battle or from where is the user in the group in which battle from GROUP O BATTLE
                //current_group_score: number => sum of the scores map from GROUP
                //last_update: last_update from GROUP
                //submission_deadline: submission_deadline => FROM BATTLE in which the group is enrolled
                //students: list of the students in the GROUP

                List<SupportClassInfoStudent> tournaments = getGroupByUsername(username);

                for(var t : tournaments){
                    Group group = t.getGroups();

                    cards.add(new CardInfoStudent(
                            //tournament_title
                            //t.getTitle(),
                            t.getTournamentTitle(),
                            //battle_title
                            t.getTitle(),
                            //current_group_score
                            group.getScores().values().stream().reduce(0, Integer::sum),
                            //last_update
                            group.getLast_update(),
                            //submission_deadline
                            t.getSubmission_deadline(),
                            //students
                            group.getMembers()
                                    .stream()
                                    .map(e -> new CardInfoStudent.Student(e.getUsername()))
                                    .toList()
                    ));
                }
            }
            case EDUCATOR -> {
                Collection<Tournament> tournaments = tournamentRepository.findTournamentsByEducator(username);

                for(var t : tournaments){
                    cards.add(new CardInfoEducator(
                            t.getTitle(),
                            t.getSubscribed_users().size(),
                            t.getBattles().size(),
                            t.getSubscription_deadline(),
                            t.getEducators()
                                    .stream()
                                    .map(e -> new CardInfoEducator.Educator(e.getUsername()))
                                    .toList()
                    ));
                }
            }
        }

        return DashboardResponse.builder()
                .account_type(accountType.name())
                .notifications(null)
                .cards(cards)
                .build();
    }

    private List<SupportClassInfoStudent> getGroupByUsername(String username){
        Criteria criteria = Criteria.where("battles.groups.members.username").is(username);

        //IDEA: db.tournament.aggregate([{$match: {$or: [{"battles.groups.leader.username": "Prova"}, {"battles.groups.members.username": "Prova"}]}}, {$unwind: "$battles"}, {$unwind: "$battles.groups"}, {$project: {"tournamentTitle": "$title", "battles.title": 1, "battles.submission_deadline": 1,"battles.groups": 1}}])
        AggregationOperation match = Aggregation.match(criteria);
        AggregationOperation unwind1 = Aggregation.unwind("battles");
        AggregationOperation unwind2 = Aggregation.unwind("battles.groups");

        AggregationOperation project1 = Aggregation.project("title","battles").and("title").as("tournamentTitle");

        AggregationOperation project2 = Aggregation.project("tournamentTitle", "battles.title", "battles.submission_deadline","battles.groups");
        Aggregation aggregation = Aggregation.newAggregation(match, unwind1, unwind2, project1, project2);
        AggregationResults<SupportClassInfoStudent> results = mongoTemplate.aggregate(aggregation, "tournament", SupportClassInfoStudent.class);

        return results.getMappedResults();
    }
}

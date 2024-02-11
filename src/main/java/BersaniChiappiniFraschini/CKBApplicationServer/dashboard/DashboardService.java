package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationDetails;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service that gathers information to construct the dashboard view for the user
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TournamentRepository tournamentRepository;
    private final UserDetailsService userDetailsService;

    private final MongoTemplate mongoTemplate;
    public DashboardResponse getDashboard() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        String username = auth.getName();

        User user = (User) userDetailsService.loadUserByUsername(username);

        // Fetch user notifications
        var notifications = user.getNotifications();

        List<CardInfo> cards = new ArrayList<>();
        switch (accountType) {
            case STUDENT -> {

                //tournament_title: from title of tournament from TOURNAMENT
                //battle_title: from title of battle or from where is the user in the group in which battle from GROUP O BATTLE
                //last_update: last_update from GROUP
                //submission_deadline: submission_deadline => FROM BATTLE in which the group is enrolled
                //students: list of the students in the GROUP
                List<GroupBattleTournamentInfo> groupBattle = getGroupsByUsername(username);

                for(var gInfo : groupBattle){
                    Group group = gInfo.getGroup();

                    cards.add(new CardInfoStudent(
                            //tournament_title
                            gInfo.getTournamentTitle(),
                            //battle_title
                            gInfo.getBattleTitle(),
                            //current_group_score
                            group.getTotal_score(),
                            // group leader
                            group.getLeader().getUsername(),
                            //last_update
                            group.getLast_update(),
                            //submission_deadline
                            gInfo.getSubmissionDeadline(),
                            //students
                            group.getMembers()
                                    .stream()
                                    .map(e -> new CardInfoStudent.Student(e.getUsername()))
                                    .toList(),
                            group.getAPI_Token()
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
                            t.is_open(),
                            t.getEducators()
                                    .stream()
                                    .map(e -> new CardInfoEducator.Educator(e.getUsername()))
                                    .toList()
                    ));
                }
            }
        }

        if (notifications == null) notifications = List.of();

        return DashboardResponse.builder()
                .account_type(accountType.name())
                .notifications(notifications.stream()
                        .filter(notification -> !notification.is_closed())
                        .map(notification -> new NotificationDetails(
                                notification.getId(),
                                notification.getMessage(),
                                notification.getType()
                        )).toList())
                .cards(cards)
                .build();
    }

    /**
     * Find the groups where the user is in.
     * @param username
     * @return the list of information necessary to show to the frontend for the groups that the user is in.
     */
    private List<GroupBattleTournamentInfo> getGroupsByUsername(String username){
        //IDEA: db.tournament.aggregate([{$match: {"battles.groups.members.username": "Uname"}}, {$unwind: "$battles"}, {$unwind: "$battles.groups"}, {$project: {"tournamentTitle": "$title", "battles.title": 1, "battles.submission_deadline": 1,"battles.groups": 1}}])
        // get tournament where the user is participating
        AggregationOperation unwind1 = Aggregation.unwind("battles");

        AggregationOperation project1 = Aggregation.project("battles").and("title").as("tournamentTitle");
        AggregationOperation project2 = Aggregation.project("tournamentTitle", "battles")
                .and("battles.title").as("battleTitle")
                .and("battles.submission_deadline").as("submissionDeadline");

        AggregationOperation unwind2 = Aggregation.unwind("battles.groups");

        AggregationOperation project3 = Aggregation.project("tournamentTitle", "battleTitle", "submissionDeadline")
                .and("battles.groups").as("group");

        Criteria criteria = Criteria.where("group.members.username").is(username);
        AggregationOperation match = Aggregation.match(criteria);

        Aggregation aggregation = Aggregation.newAggregation(unwind1, project1, project2, unwind2, project3, match);
        AggregationResults<GroupBattleTournamentInfo> results = mongoTemplate.aggregate(aggregation, "tournament", GroupBattleTournamentInfo.class);
        
        return results.getMappedResults();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GroupBattleTournamentInfo {
        private String tournamentTitle;
        private String battleTitle;
        private Date submissionDeadline;
        private Group group;
    }
}

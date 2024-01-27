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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

                //Collection<Battle> battles = tournamentRepository.findTournamentsByStudent(username);

                //tournament_title: from title of tournament from TOURNAMENT
                //battle_title: from title of battle or from where is the user in the group in which battle from GROUP O BATTLE
                //current_group_score: number => sum of the scores map from GROUP
                //last_update: last_update from GROUP
                //submission_deadline: submission_deadline => FROM BATTLE in which the group is enrolled
                //students: list of the students in the GROUP

                /*
                    I'll do the search in the group and integrate with the other information

                    RICERCA DEL GROUPPO IN CUI SI TROVA UTENTE
                        -> è IL LEADER
                        -> O NELLA LISTA UTENTI

                    POSSO FARE LA QUERY E RITORNA TUTTO IN TOURNAMENT OGGETTO
                    E POI ESTRAGGO IL VALORE

                    IDEA: db.games.aggregate([{$match: {"reviews.user.products": 17}}, {$unwind: "$reviews"}, {$unwind: "$reviews.user"}])
                */

                // TODO TEST
                List<Tournament> tournaments = getGroupByUsername(username);


                for(var t : tournaments){
                    Group group = t.getBattles().get(0).getGroups().get(0);
                    Battle battle = t.getBattles().get(0);

                    cards.add(new CardInfoStudent(
                            //tournament_title
                            t.getTitle(),
                            //battle_title
                            battle.getTitle(),
                            //current_group_score
                            group.getScores().values().stream().reduce(0, Integer::sum),
                            //last_update
                            group.getLast_update(),
                            //submission_deadline
                            battle.getSubmission_deadline(),
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

    private List<Tournament> getGroupByUsername(String username){
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("battles.groups.leader.username").is(username),
                Criteria.where("battles.groups.members.username").is(username)
        );

        //IDEA: db.games.aggregate([{$match: {"reviews.user.products": 17}}, {$unwind: "$reviews"}, {$unwind: "$reviews.user"}])
        AggregationOperation match = Aggregation.match(criteria);
        AggregationOperation unwind1 = Aggregation.unwind("battles");
        AggregationOperation unwind2 = Aggregation.unwind("battles.groups");
        Aggregation aggregation = Aggregation.newAggregation(match, unwind1, unwind2);
        AggregationResults<Tournament> results = mongoTemplate.aggregate(aggregation, "tournament", Tournament.class);

        return results.getMappedResults();
    }
}

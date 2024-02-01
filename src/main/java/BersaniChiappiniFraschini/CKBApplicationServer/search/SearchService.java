package BersaniChiappiniFraschini.CKBApplicationServer.search;


import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository ;
    private final MongoTemplate mongoTemplate;

    public List<TournamentInfo> searchTournament(String tournamentTitle){
        Collection<Tournament> tournaments = tournamentRepository.findByTitleSearch(tournamentTitle);
        return buildTournamentsInfo(tournaments);
    }

    public List<BattleInfo> searchBattle(String battleTitle){

        AggregationOperation unwind = Aggregation.unwind("battles");
        AggregationOperation match = Aggregation.match(
                Criteria.where("battles.title").regex(battleTitle) // "i" per rendere la ricerca case-insensitive
        );

        AggregationOperation project1 = Aggregation.project("title","battles").and("title").as("tournamentTitle");
        AggregationOperation project2 = Aggregation.project("tournamentTitle", "battles.title", "battles.enrollment_deadline","battles.groups");
        Aggregation aggregation = Aggregation.newAggregation(unwind, match, project1, project2);

        AggregationResults<SupportClassBattleInfo> results = mongoTemplate.aggregate(aggregation, "tournament", SupportClassBattleInfo.class);

        return buildBattlesInfo(results);
    }

    private List<BattleInfo> buildBattlesInfo(AggregationResults<SupportClassBattleInfo> results){

        List<BattleInfo> battlesInfo = new ArrayList<>();
        Date today = new Date();

        for(var b : results){
            battlesInfo.add(new BattleInfo(
                    b.getTournamentTitle(),
                    b.getTitle(),
                    !today.after(b.getEnrollment_deadline()),
                    b.getEnrollment_deadline(),
                    b.getGroups().size()
            ));
        }

        return battlesInfo;
    }

    private List<TournamentInfo> buildTournamentsInfo(Collection<Tournament> tournaments){
        List<TournamentInfo> tournamentInfos = new ArrayList<>();

        for(var t : tournaments){
            tournamentInfos.add(new TournamentInfo(
                    t.getTitle(),
                    t.getSubscribed_users().size(),
                    t.getBattles().size(),
                    t.getSubscription_deadline(),
                    t.getEducators()
                            .stream()
                            .map(e -> new TournamentInfo.Educator(e.getUsername()))
                            .toList(),
                    t.is_open()
            ));
        }

        return tournamentInfos;
    }

    public List<String> searchUser(String username){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());

        Collection<User> users = userRepository.findByAccountTypeAndUsernameLike(accountType, username);
        return users.stream().map(User::getUsername).toList();
    }

    @Data
    @AllArgsConstructor
    private static class SupportClassBattleInfo {
        private String tournamentTitle;
        private String title;
        private Date enrollment_deadline;
        private List<Group> groups;
    }
}

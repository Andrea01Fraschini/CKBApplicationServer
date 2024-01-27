package BersaniChiappiniFraschini.CKBApplicationServer.search;


import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.dashboard.CardInfoEducator;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentCreationRequest;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TournamentRepository tournamentRepository;
    private final MongoTemplate mongoTemplate;


    public List<TournamentInfo> searchTournaments(){
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentInfo> tournamentInfo = getTournament(tournaments);

        return tournamentInfo;
    }

    public List<TournamentInfo> searchTournament(String tournamentTitle){
        Collection<Tournament> tournaments = tournamentRepository.findByTitleSearch(tournamentTitle);

        List<TournamentInfo> tournamentInfo = getTournament(tournaments);

        return tournamentInfo;
    }

    /*
    public List<BattleInfo> searchBattlesAll(){

        AggregationOperation unwind = Aggregation.unwind("battles");
        AggregationOperation project = Aggregation.project("battles");
        Aggregation aggregation = Aggregation.newAggregation(unwind, project);
        AggregationResults<Battle> results = mongoTemplate.aggregate(aggregation, "tournament", Battle.class);

        List<BattleInfo> battlesInfo = getBattle(results);
    }

    public List<BattleInfo> searchBattle(String battleTitle){

        AggregationOperation unwind = Aggregation.unwind("battles");
        AggregationOperation match = Aggregation.match(
                Aggregation.matchOperation(
                        Aggregation.regex("battles.title", battleTitle, "i")  // "i" per rendere la ricerca case-insensitive
                )
        );
        AggregationOperation project = Aggregation.project("battles");
        Aggregation aggregation = Aggregation.newAggregation(unwind, match, project);
        AggregationResults<Battle> results = mongoTemplate.aggregate(aggregation, "tournament", Battle.class);

        List<BattleInfo> battlesInfo = getBattle(results);
    }

    private List<BattleInfo> getBattle( AggregationResults<Battle> results){

         List<BattleInfo> battlesInfo = new ArrayList<>();

        // I'm waiting for Battle Model
        for(var b : results){
            battlesArray.add(new BattleInfo(
                    b.getTitle(),
                    b.getStatus(),
                    b.getEnrollment_deadline(),
                    b.getEnrolledGroups().size()
            ));
        }

        return battlesInfo;
    }

    */


    private List<TournamentInfo> getTournament( Collection<Tournament> tournaments){
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
}

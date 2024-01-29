package BersaniChiappiniFraschini.CKBApplicationServer.search;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    @GetMapping("/tournaments")
    public List<TournamentInfo> getTournaments(){
        return searchService.searchTournaments();
    }

    @GetMapping("/tournament")
    public List<TournamentInfo> getTournaments( @RequestParam(value = "tournamentTitle") String tournamentTitle){

        return searchService.searchTournament(tournamentTitle);
    }


    @GetMapping("/battles")
    public List<BattleInfo> getBattlesAll(){

        return searchService.searchBattlesAll();
    }


    @GetMapping("/battle")
    public List<BattleInfo> getBattlesAll(@RequestParam(value = "battleTitle") String battleTitle){

        return searchService.searchBattle(battleTitle);
    }

    @GetMapping("/user")
    public List<String> getUser(@RequestParam(value = "username") String username){
        return searchService.searchUser(username);
    }

}

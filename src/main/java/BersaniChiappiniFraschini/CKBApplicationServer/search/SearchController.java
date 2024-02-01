package BersaniChiappiniFraschini.CKBApplicationServer.search;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tournament")
    public List<TournamentInfo> searchTournament(
            @RequestParam(value = "tournamentTitle") String tournamentTitle
    ){
        return searchService.searchTournament(tournamentTitle);
    }

    @GetMapping("/battle")
    public List<BattleInfo> searchBattle(
            @RequestParam(value = "battleTitle") String battleTitle
    ){
        return searchService.searchBattle(battleTitle);
    }

    @GetMapping("/user")
    public List<String> getUser(
            @RequestParam(value = "username"
    ) String username){
        return searchService.searchUser(username);
    }

}

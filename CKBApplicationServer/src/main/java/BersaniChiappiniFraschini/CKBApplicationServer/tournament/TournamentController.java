package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;

    @PostMapping("/create")
    public ResponseEntity<PostResponse> createTournament(
            @RequestBody TournamentCreationRequest request
    ){
        return tournamentService.createTournament(request);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<PostResponse> subscribeTournament(
            @RequestBody TournamentSubscribeRequest request
    ){

        return tournamentService.subscribeTournament(request);
    }

    //TODO: here the view of the tournament in detail
    @GetMapping
    public ResponseEntity<TournamentGetResponse> getTournament(
            @RequestParam String tournamentTitle
    ){

        return tournamentService.getTournament(tournamentTitle);
    }
}

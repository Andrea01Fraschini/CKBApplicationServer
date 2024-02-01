package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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

    //here the view of the tournament in detail
    @GetMapping("/view")
    public ResponseEntity<TournamentGetResponse> getTournament(
            @RequestParam String tournamentTitle
    ){

        return tournamentService.getTournament(tournamentTitle);
    }

    @PostMapping ("/close")
    public ResponseEntity<PostResponse> closeTournament(
            @RequestParam String tournamentTitle
    ){

        return tournamentService.closeTournament(tournamentTitle);
    }

    @GetMapping("/list")
    public ResponseEntity<List<TournamentsListEntry>> getTournamentsList() {
        return tournamentService.getTournamentsList();
    }

    public record TournamentsListEntry(
            String title,
            boolean is_open,
            Date subscription_deadline,
            int subscribed_students,
            List<String> educators
    ) {}
}

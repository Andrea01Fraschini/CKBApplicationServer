package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;
    @PostMapping("/create")
    public ResponseEntity<TournamentCreationResponse> createTournament(
            @RequestBody TournamentCreationRequest request
    ){
        return tournamentService.createTournament(request);
    }
}

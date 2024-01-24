package BersaniChiappiniFraschini.CKBApplicationServer.tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final UserDetailsService userDetailsService;
    public ResponseEntity<TournamentCreationResponse> createTournament(TournamentCreationRequest request){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new TournamentCreationResponse("Cannot create tournament as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var title = request.getTitle();

        if(tournamentRepository.existsByTitle(title)){
            var res = new TournamentCreationResponse("Tournament with title %s already exists".formatted(title));;
            return ResponseEntity.badRequest().body(res);
        }

        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var educator = (User) userDetailsService.loadUserByUsername(username);
        var subscription_deadline = request.getSubscription_deadline();

        Tournament tournament = Tournament.builder()
                .title(title)
                .subscription_deadline(subscription_deadline)
                .is_open(true)
                .educators(List.of(educator))
                .subscribed_users(List.of())
                .battles(List.of())
                .build();

        tournamentRepository.insert(tournament);

        // for each user in request.invited_managers, send invite request
        // send notification to all students

        return ResponseEntity.ok(new TournamentCreationResponse());
    }
}

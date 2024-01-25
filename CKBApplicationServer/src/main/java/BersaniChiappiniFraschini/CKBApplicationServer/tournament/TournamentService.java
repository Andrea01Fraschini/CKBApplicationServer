package BersaniChiappiniFraschini.CKBApplicationServer.tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
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
    private final NotificationService notificationService;

    public ResponseEntity<PostResponse> createTournament(TournamentCreationRequest request){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new PostResponse("Cannot create tournament as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var title = request.getTitle();

        if(tournamentRepository.existsByTitle(title)){
            var res = new PostResponse("Tournament with title %s already exists".formatted(title));
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

        notificationService.sendTournamentCreationNotifications(tournament);

        // for each user in request.invited_managers, send invite request

        return ResponseEntity.ok(null);
    }
}

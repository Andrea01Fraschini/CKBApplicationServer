package BersaniChiappiniFraschini.CKBApplicationServer.tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that manages tournaments (will become big)
 */
@Service
@RequiredArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final UserDetailsService userDetailsService;
    private final NotificationService notificationService;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<PostResponse> createTournament(TournamentCreationRequest request){

        // Check for privileges
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new PostResponse("Cannot create tournament as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var title = request.getTitle();

        // Check if duplicate
        if(tournamentRepository.existsByTitle(title)){
            var res = new PostResponse("Tournament with title %s already exists".formatted(title));
            return ResponseEntity.badRequest().body(res);
        }

        // Fetch creator's information
        var username = auth.getName();
        var educator = (User) userDetailsService.loadUserByUsername(username);
        var subscription_deadline = request.getSubscription_deadline();

        // Create new tournament
        Tournament tournament = Tournament.builder()
                .title(title)
                .subscription_deadline(subscription_deadline)
                .is_open(true)
                .educators(List.of(educator))
                .subscribed_users(List.of())
                .battles(List.of())
                .build();

        tournamentRepository.insert(tournament);

        // Notify the whole world about this
        // TODO Run this in a different thread
        notificationService.sendTournamentCreationNotifications(tournament);

        // for each user in request.invited_managers, send invite request

        return ResponseEntity.ok(null);
    }

    public void addBattle(String tournament_title, Battle battle) {
        var update = new Update();
        update.push("battles", battle);
        var criteria = Criteria.where("title").in(tournament_title);
        mongoTemplate.updateFirst(Query.query(criteria), update, "tournament");
    }
}

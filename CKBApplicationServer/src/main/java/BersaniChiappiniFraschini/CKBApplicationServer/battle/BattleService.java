package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final TournamentRepository tournamentRepository;
    private final TournamentService tournamentService;
    private final NotificationService notificationService;

    public ResponseEntity<PostResponse> createBattle(BattleCreationRequest request) {

        // Check for privileges
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if (accountType != AccountType.EDUCATOR) {
            var res = new PostResponse("Cannot create a battle as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var tournament_title = request.getTournament_title();
        var battle_title = request.getBattle_title();
        // Fetch tournament context
        var tournament = tournamentRepository.findTournamentByTitle(tournament_title);

        // Check if duplicate
        if (tournament.getBattles()
                .stream()
                .anyMatch(battle -> battle_title.equals(battle.getTitle())))
        {
            var res = new PostResponse("Battle with title %s already exists".formatted(battle_title));
            return ResponseEntity.badRequest().body(res);
        }

        var min_size = request.getMin_group_size();
        var max_size = request.getMax_group_size();
        var description = request.getDescription();
        var enrollment_deadline = request.getEnrollment_deadline();
        var submission_deadline = request.getSubmission_deadline();
        var manual_evaluation = request.isManual_evaluation();
        var eval_parameters = request.getEvaluation_parameters();

        // Create new battle
        Battle battle = Battle.builder()
                .title(battle_title)
                .min_group_size(min_size)
                .max_group_size(max_size)
                .description(description)
                .enrollment_deadline(enrollment_deadline)
                .submission_deadline(submission_deadline)
                .manual_evaluation(manual_evaluation)
                .evaluation_parameters(List.of()) // TODO: put actual evaluation parameters
                .groups(List.of())
                .build();

        // TODO: register timed event to event handler

        // update tournament
        tournamentService.addBattle(tournament_title, battle);

        // Notify subscribed students
        // TODO: run in different thread (?)
        notificationService.sendBattleCreationNotification(battle, tournament);

        return ResponseEntity.ok(null);
    }
}

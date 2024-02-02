package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
import BersaniChiappiniFraschini.CKBApplicationServer.search.BattleInfo;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final InviteService inviteService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final ScoreService scoreService;

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
                .educators(List.of(new TournamentManager(educator)))
                .subscribed_users(List.of())
                .battles(List.of())
                .educator_creator(username)
                .build();

        tournamentRepository.insert(tournament);

        // Notify the whole world about this
        Runnable taskSendEmail = () -> notificationService.sendTournamentCreationNotifications(tournament);
        executor.submit(taskSendEmail);

        // for each user in request.invited_managers, send invite request
        for (var invitee : request.getInvited_managers()) {
            var manager = (User) userDetailsService.loadUserByUsername(invitee);
            inviteService.sendManagerInvite(educator, manager, tournament);
        }

        return ResponseEntity.ok(null);
    }

    public ResponseEntity<PostResponse> subscribeTournament(TournamentSubscribeRequest request){

        var tournament = tournamentRepository.findTournamentByTitle(request.getTitle());
        // Check if tournament subscription deadline has expired
        if (tournament.getSubscription_deadline().before(new Date())) {
            var res = new PostResponse("Cannot subscribe to tournament, subscription deadline expired");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();

        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.STUDENT){
            var res = new PostResponse("Cannot subscribe to tournament as educator");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        String username = auth.getName();
        var title = request.getTitle();

        // I check if the user is already subscribed to the tournament
        Optional<Tournament> tournament_match = tournamentRepository.findBySubscribed_user(username, title);
        if(tournament_match.isPresent()){
            var res = new PostResponse("Already subscribed in this tournament");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        //Add the user to the subscribed field of the tournament
        User user = (User) userDetailsService.loadUserByUsername(username);

        var update = new Update();
        update.push("subscribed_users", new TournamentSubscriber(user));
        var criteria = Criteria.where("title").in(title);
        mongoTemplate.updateFirst(Query.query(criteria), update, "tournament");

        //send e-mail of the subscription
        Runnable taskSendEmail = () -> notificationService.sendSuccessfulTournamentSubscription(user, tournament);
        executor.submit(taskSendEmail);

        return ResponseEntity.ok(null);
    }

    public void addBattle(String tournament_title, Battle battle) {
        var update = new Update();
        update.push("battles", battle);
        var criteria = Criteria.where("title").in(tournament_title);
        mongoTemplate.updateFirst(Query.query(criteria), update, "tournament");
    }

    public ResponseEntity<TournamentGetResponse> getTournament(String tournamentTitle){
        Tournament tournament = tournamentRepository.findTournamentByTitle(tournamentTitle);

        if(tournament == null){
            new ResponseEntity<>(new PostResponse("Tournament doesn't found"), HttpStatus.BAD_REQUEST);
        }

        List<BattleInfo> battleInfos = new ArrayList<>();
        Date today = new Date();

        for(Battle b : tournament.getBattles()){
            battleInfos.add(
                    new BattleInfo(
                            tournamentTitle,
                            b.getTitle(),
                            !today.after(b.getSubmission_deadline()),
                            b.getEnrollment_deadline(),
                            b.getGroups().size()
                    )
            );
        }

        // get user info
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();
        var username = auth.getName();

        TournamentGetResponse tournamentGetResponse = TournamentGetResponse.builder()
                .is_open(tournament.is_open())
                .creator(tournament.getEducator_creator())
                .managers(tournament.getEducators().stream().map(TournamentManager::getUsername).toList())
                .battles(battleInfos)
                .leaderboard(tournament.getLeaderboard())
                .already_subscribed(tournament.getSubscribed_users()
                        .stream()
                        .anyMatch(subscriber -> subscriber.getUsername().equals(username)))
                .build();

        return new ResponseEntity<>(tournamentGetResponse, HttpStatus.ACCEPTED);
    }

    public ResponseEntity<PostResponse> closeTournament(String tournamentTitle){
        //check if an educator
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();

        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if(accountType != AccountType.EDUCATOR){
            var res = new PostResponse("Cannot close a tournament as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        // check if all battles are closed
        Tournament tournament = tournamentRepository.findTournamentByTitle(tournamentTitle);
        List<Battle> battles = tournament.getBattles();
        Date date = new Date();

        for(Battle b : battles){
            if(!date.after(b.getSubmission_deadline())){
                PostResponse postResponse = new PostResponse("Not all battles are closed");
                return ResponseEntity.badRequest().body(postResponse);
            }
        }

        // remember a student can play in several battle

        // here update badges, we don't have to implement them
        // TODO: scoreService.updateScores(tournament);

        for(TournamentSubscriber u : tournament.getSubscribed_users()) {
            Runnable taskSendEmail = () -> notificationService.sendGlobalRanksAvailable(u.getEmail(), tournamentTitle);
            executor.submit(taskSendEmail);
        }

        // Update in database
        var criteria = Criteria.where("title").is(tournamentTitle);
        var update = new Update().set("is_open", false);
        mongoTemplate.updateFirst(Query.query(criteria), update, Tournament.class);

        return ResponseEntity.ok().body(null);
    }

    public ResponseEntity<List<TournamentController.TournamentsListEntry>> getTournamentsList() {
        var tournaments = tournamentRepository.findAll(); // not good for large scale requests
        return ResponseEntity.ok(
                tournaments.stream().map(t -> new TournamentController.TournamentsListEntry(
                        t.getTitle(),
                        t.is_open(),
                        t.getSubscription_deadline(),
                        t.getSubscribed_users().size(),
                        t.getEducators().stream().map(TournamentManager::getUsername).toList()
                )).toList()
        );
    }


}

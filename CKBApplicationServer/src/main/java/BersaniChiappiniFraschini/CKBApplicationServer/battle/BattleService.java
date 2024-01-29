package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.TimedEvent;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.search.SearchService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class BattleService {
    private final TournamentRepository tournamentRepository;
    private final MongoTemplate mongoTemplate;
    private final TournamentService tournamentService;
    private final NotificationService notificationService;
    private final UserDetailsService userDetailsService;
    private final InviteService inviteService;
    private final EventService eventService;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

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
                .id(ObjectId.get().toString())
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

        // Register battle start event
        eventService.registerTimedEvent(
                new TimedEvent("new battle", enrollment_deadline),
                startBattle(tournament, battle)
        );

        // update tournament
        tournamentService.addBattle(tournament_title, battle);

        // Notify subscribed students
        Runnable taskSendEmail = () -> notificationService.sendBattleCreationNotification(battle, tournament);
        executor.submit(taskSendEmail);

        return ResponseEntity.ok(null);
    }

    public ResponseEntity<PostResponse> enrollGroup(BattleEnrollmentRequest request) {

        // Check for privileges
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if (accountType != AccountType.STUDENT) {
            var res = new PostResponse("Cannot enroll in a battle as educator");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var tournament_title = request.getTournament_title();
        var battle_title = request.getBattle_title();
        var invites = request.getInvited_members()
                .stream()
                .map(name -> (User) userDetailsService.loadUserByUsername(name))
                .toList();

        var tournament = tournamentRepository.findTournamentByTitle(tournament_title);
        var battle_match = tournament.getBattles()
                .stream()
                .filter(b -> battle_title.equals(b.getTitle()))
                .findFirst();

        // Check if group can enroll
        if (battle_match.isEmpty()) {
            var res = new PostResponse("No battle found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        // Fetch creator information
        var username = auth.getName();
        var student = (User) userDetailsService.loadUserByUsername(username);
        var battle = battle_match.get();

        // TODO: check if user is in another group for the battle

        if (new Date(System.currentTimeMillis()).after(battle.getEnrollment_deadline())) {
            var res = new PostResponse("Enrollment period for battle closed");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        // + 1 because of the creator
        if (invites.size() + 1 < battle.getMin_group_size() || invites.size() + 1 > battle.getMax_group_size()) {
            var message = "Battle group limits exceeded, group size must be between %d and %d"
                    .formatted(battle.getMin_group_size(), battle.getMax_group_size());
            var res = new PostResponse(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        // Create group
        Group group = Group.builder()
                .id(ObjectId.get().toString())
                .leader(new GroupMember(student))
                .members(List.of(new GroupMember(student)))
                .pending_invites(invites.stream().map(PendingInvite::new).toList())
                .scores(new HashMap<>()) // TODO: create map from battle evaluation parameters
                .repository(null)
                .build();

        // Send invites
        for (var invitee : invites) {
            inviteService.sendGroupInvite(student, invitee, tournament, battle, group);
        }

        // Update collection
        var criteria = Criteria.where("title").is(tournament.getTitle())
                .and("battles.title").is(battle.getTitle());
        var update = new Update();
        update.push("battles.$.groups", group);
        mongoTemplate.updateFirst(Query.query(criteria), update, "tournament");

        //send notification of registration to the battle
        Runnable taskSendEmail = () -> notificationService.sendNotification(student.getEmail(), "You have successfully enrolled in the " + "'%s'".formatted(battle.getTitle()) + " battle");
        executor.submit(taskSendEmail);

        return ResponseEntity.ok(null);
    }

    public Runnable startBattle(Tournament tournament, Battle battle) {
        // TODO: automatic deletion of pending invites (can be omitted ?)
        return () -> {
            // TODO: Call GitHubManager to create repository
            var repositoryUrl = "";

            var query = Query.query(
                    Criteria.where("_id")
                            .is(new ObjectId(tournament.getId()))
                            .and("battles._id")
                            .is(new ObjectId(battle.getId()))
            );
            var update = new Update().set("repository", repositoryUrl);
            mongoTemplate.updateFirst(query, update, "tournament");

            for (var group : battle.getGroups()) {
                // TODO: Generate API token
                var token = "";
                Runnable taskSendEmail = () -> notificationService.sendRepositoryInvites(group, battle, token);
                executor.submit(taskSendEmail);
            }
        };
    }

    // The tournamentTitle is the tournament's title in which I can find the battleTitle
    public ResponseEntity<Object> getBattle(String tournamentTitle, String battleTitle){
        // look if is a student or educator

        /*
        for both
        leaderBoard: List of leader with the point of the group
        generale information battle:
            - title
            - repository
            - description
            - language
            - evaluation_parameters
            - manual_evaluation
            - submission_deadline
            - enrollment_deadline
            - min_group_size
            - max_group_size
            - tests
        */

        AggregationOperation match = Aggregation.match(
                Criteria.where("title").is(tournamentTitle)
        );
        AggregationOperation unwind = Aggregation.unwind("battles");
        AggregationOperation match2 = Aggregation.match(
                Criteria.where("battles.title").is(battleTitle)
        );
        AggregationOperation project1 = Aggregation.project("battles");
        Aggregation aggregation = Aggregation.newAggregation(match, unwind, match2, project1);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "tournament", Map.class);

        if(results.getMappedResults().size() == 0){
            return new ResponseEntity<>(new PostResponse("Battle not found"), HttpStatus.BAD_REQUEST);
        }

        Battle battle = (Battle) results.getMappedResults().get(0).get("battles");
        List<Group> groups = battle.getGroups();

        BattleInfoResponse battleInfoResponse = BattleInfoResponse.builder()
                .battle(battle)
                .build();

        for (Group g : groups){
            Map<String, Integer> score = g.getScores();
            int sum = score.values().stream().mapToInt(Integer::intValue).sum();
            String leader = g.getLeader().getUsername();
            battleInfoResponse.addScore(leader, sum);
        }


        // for student battle detail
        /*
        Group info in which is the student info of the student
            in group_info:
                - members [username, url, boolean leader]
                - API_Token
                - total_score
                - timeless_score
                - manual_evaluation_score
                - tests
                - list of pending invites
         */

        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());


        var username = auth.getName();

        switch (accountType) {
            case STUDENT -> {
                Group myGroup = null;

                // IDENTIFY MY GROUP
                for(Group g : groups){
                   for(GroupMember gr : g.getMembers()){
                       if(gr.getUsername().equals(username)){
                           myGroup = g;
                           break;
                       }
                   }

                   if (myGroup != null) break;
                }

                if(myGroup != null){
                    int totalScore = battleInfoResponse.getScore(myGroup.getLeader().getUsername());

                    // scruttura battle + gruppo + totalscore
                    BattleInfoStudent battleInfoStudent = new BattleInfoStudent(myGroup, totalScore, battle, battleInfoResponse.getLeaderboard());

                    return new ResponseEntity<>(battleInfoStudent, HttpStatus.ACCEPTED);
                }else{
                    BattleInfoStudent battleInfoStudent = new BattleInfoStudent();
                    battleInfoStudent.setBattle(battle);
                    return new ResponseEntity<>(battleInfoStudent, HttpStatus.ACCEPTED);
                }
            }
            // for educator the list of groups
            /*
            Information groups
                - name group
                - evaluation_status
                - score for group end type of evaluation
             */
            case EDUCATOR -> {
                //TODO: I'M WAITING FOR TYPE OF EVALUTATION
                return new ResponseEntity<>("TO DO", HttpStatus.ACCEPTED);
            }
        }

        return new ResponseEntity<>(new PostResponse("Not found ACCOUNT TYPE"), HttpStatus.BAD_REQUEST);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BattleInfoStudent{
            private Group group;
            private int total_score;
            private Battle battle;

            private Map<String, Integer> pointGroups;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BattleInfoEducator{
        private List<Group> groups;
        private Battle battle;
        private Map<String, Integer> pointGroups;
    }
}
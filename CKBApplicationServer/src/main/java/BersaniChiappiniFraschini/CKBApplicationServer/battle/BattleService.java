package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.TimedEvent;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.*;
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

import java.util.*;
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

    private final AuthenticationService authenticationService;

    private final GitHubManagerService gitHubManagerService;

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

        boolean control = false;
        // Check for permissions in that tournament to create battle
        for(TournamentManager t : tournament.getEducators()){
            if(t.getUsername().equals(auth.getName()) || auth.getName().equals(tournament.getEducator_creator())){
                control = true;
                break;
            }
        }

        if(!control){
            var res = new PostResponse("You don't have the permission to create the battle");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        // Check if duplicate
        if (tournament.getBattles()
                .stream()
                .anyMatch(battle -> battle_title.equals(battle.getTitle()))) {
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

        // add the repository for the battle
        String repository = gitHubManagerService.createRepository(tournament.getTitle(), battle_title, description);

        // TODO: upload the file of the project

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
                .repository(repository)
                .build();

        // Register battle start event
        EventService.registerTimedEvent(
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

        String id = ObjectId.get().toString();
        //TODO: to manage error
        String token = authenticationService.generateToken(id);

        //TODO: how to get the file of the project?

        // Create group
        Group group = Group.builder()
                .id(id)
                .leader(new GroupMember(student))
                .members(List.of(new GroupMember(student)))
                .pending_invites(invites.stream().map(PendingInvite::new).toList())
                .scores(new HashMap<EvalParameter, Integer>())
                //The repository of the group to do the download (fork)
                .repository("")
                .API_Token(token)
                .done_manual_evaluation(false)
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
        // automatic deletion of pending invites (can be omitted ?)
        List<Group> gourpsUpdate = automaticControl(tournament, battle);

        return () -> {
            /*
            var repositoryUrl = battleNew.getRepository();

            var query = Query.query(
                    Criteria.where("_id")
                            .is(new ObjectId(tournament.getId()))
                            .and("battles._id")
                            .is(new ObjectId(battleNew.getId()))
            );
            var update = new Update().set("repository", repositoryUrl);
            mongoTemplate.updateFirst(query, update, "tournament");
            */

            for (var group : gourpsUpdate) {
                String token = group.getAPI_Token();
                Runnable taskSendEmail = () -> notificationService.sendRepositoryInvites(group, battle, token);
                executor.submit(taskSendEmail);
            }
        };
    }

    private List<Group> automaticControl(Tournament tournament, Battle battle){
        List<Group> groups = battle.getGroups();
        Iterator<Group> iterator = groups.iterator();
        List<TournamentSubscriber> tournamentSubscribers = tournament.getSubscribed_users();

        while (iterator.hasNext()) {
            Group group = iterator.next();

            // Condizione per rimuovere l'elemento
            if (group.getMembers().size() < battle.getMin_group_size() || group.getMembers().size() > battle.getMax_group_size()) {

                Runnable taskSendEmail = () -> notificationService.sendEliminationGroup(group, battle);
                executor.submit(taskSendEmail);

                iterator.remove();
            }else {
                group.getPending_invites().clear();

                // check if a Student in a group member the same student must be in the subscribed student of the tournament
                List<GroupMember> member = group.getMembers();

                // work on the id
                for(GroupMember g : member){
                    String id = g.getId();
                    if(!searchList(tournamentSubscribers, id)){
                        tournamentSubscribers.add(new TournamentSubscriber(g.getId(), g.getUsername(), g.getEmail(), 0));
                    }
                }
            }
        }

        // save the changes in the db
        var query = Query.query(
                Criteria.where("_id")
                        .is(new ObjectId(tournament.getId()))
                        .and("battles._id")
                        .is(new ObjectId(battle.getId()))
        );
        var update = new Update().set("groups", groups);

        mongoTemplate.updateFirst(query, update, "tournament");

        var query2 = Query.query(
                Criteria.where("_id")
                        .is(new ObjectId(tournament.getId()))
        );

        var update2 = new Update().set("subscribed_users", tournamentSubscribers);
        mongoTemplate.updateFirst(query2, update2, "tournament");

        return groups;
    }

    private boolean searchList(List<TournamentSubscriber> tournamentSubscribers, String id){
        for(TournamentSubscriber t : tournamentSubscribers) {
            if (t.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
    // The tournamentTitle is the tournament's title in which I can find the battleTitle
    public ResponseEntity<Object> getBattle(String tournamentTitle, String battleTitle) {
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

        if (results.getMappedResults().size() == 0) {
            return new ResponseEntity<>(new PostResponse("Battle not found"), HttpStatus.BAD_REQUEST);
        }

        Battle battle = (Battle) results.getMappedResults().get(0).get("battles");
        List<Group> groups = battle.getGroups();

        BattleInfoResponseGeneral battleInfoResponseGeneral = BattleInfoResponseGeneral.builder()
                .battle(battle)
                .build();

        for (Group g : groups) {
            Map<EvalParameter, Integer> score = g.getScores();
            int sum = score.values().stream().mapToInt(Integer::intValue).sum();
            String leader = g.getLeader().getUsername();
            battleInfoResponseGeneral.addScore(leader, sum);
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

        BattleInfo battleInfo = new BattleInfo();
        battleInfo.setTitle(battleTitle);
        battleInfo.setRepository(battle.getRepository());
        battleInfo.setMin_group_size(battle.getMin_group_size());
        battleInfo.setMax_group_size(battle.getMax_group_size());
        battleInfo.setManual_evaluation(battle.isManual_evaluation());
        battleInfo.setEnrollment_deadline(battle.getEnrollment_deadline());
        battleInfo.setSubmission_deadline(battle.getSubmission_deadline());
        battleInfo.setEvaluation_parameters(battle.getEvaluation_parameters());

        switch (accountType) {
            case STUDENT -> {
                Group myGroup = null;

                // IDENTIFY MY GROUP
                for (Group g : groups) {
                    for (GroupMember gr : g.getMembers()) {
                        if (gr.getUsername().equals(username)) {
                            myGroup = g;
                            break;
                        }
                    }

                    if (myGroup != null) break;
                }

                if (myGroup != null) {
                    int totalScore = battleInfoResponseGeneral.getScore(myGroup.getLeader().getUsername());

                    // struttura battle + gruppo + totalscore
                    BattleInfoStudent battleInfoStudent = new BattleInfoStudent(myGroup, totalScore, battleInfo, battleInfoResponseGeneral.getLeaderboard());

                    return new ResponseEntity<>(battleInfoStudent, HttpStatus.ACCEPTED);
                } else {
                    BattleInfoStudent battleInfoStudent = new BattleInfoStudent();
                    battleInfoStudent.setBattle(battleInfo);
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
                BattleInfoEducator battleInfoEducator = new BattleInfoEducator();
                battleInfoEducator.setBattle(battleInfo);

                battleInfoEducator.setGroups(battle.getGroups());

                battleInfoEducator.setLeaderBoard(battleInfoResponseGeneral.getLeaderboard());
                return new ResponseEntity<>(battleInfoEducator, HttpStatus.ACCEPTED);
            }
        }

        return new ResponseEntity<>(new PostResponse("Not found ACCOUNT TYPE"), HttpStatus.BAD_REQUEST);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BattleInfoStudent {
        private Group group;
        private int total_score;
        private BattleInfo battle;

        private Map<String, Integer> pointGroups;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BattleInfoEducator {
        private List<Group> groups;
        private BattleInfo battle;
        private Map<String, Integer> leaderBoard;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private class BattleInfo {
        private String title;
        private int min_group_size;
        private int max_group_size;
        private String repository;
        private Date enrollment_deadline;
        private Date submission_deadline;
        private boolean manual_evaluation;
        private List<EvalParameter> evaluation_parameters;
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.TimedEvent;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final JwtService jwtService;
    private final ScoreService scoreService;
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
        var project_language = request.getProject_language();
        var tests_file = request.getTests_file_name();

        // add the repository for the battle

        // upload the file of the project
        String repo = "";
        try {
            repo = gitHubManagerService.createRepository(tournament_title, battle_title, description);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PostResponse("GitHub wasn't able to create the repo, try again "+ e.getMessage()));
        }


        // Create new battle
        Battle battle = Battle.builder()
                .id(ObjectId.get().toString())
                .title(battle_title)
                .min_group_size(min_size)
                .max_group_size(max_size)
                .description(description)
                .enrollment_deadline(enrollment_deadline)
                .submission_deadline(submission_deadline)
                .evaluation_parameters(eval_parameters)
                .manual_evaluation(manual_evaluation)
                .groups(List.of())
                .repository(repo)
                .project_language(project_language)
                .tests_file_name(tests_file)
                .build();

        // Register battle start event
        EventService.registerTimedEvent(
                new TimedEvent("new battle", enrollment_deadline),
                startBattle(tournament, battle)
        );

        // Register battle close event
        EventService.registerTimedEvent(
                new TimedEvent("close battle", submission_deadline),
                closeBattle(tournament.getTitle(), battle.getTitle())
        );

        // update tournament
        tournamentService.addBattle(tournament_title, battle);

        // Notify subscribed students
        Runnable taskSendEmail = () -> notificationService.sendBattleCreationNotification(battle, tournament);
        executor.submit(taskSendEmail);

        // upload File
        gitHubManagerService.saveFileAndCreateRepository(request.getFile(), battle_title, repo);

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

        // Fetch creator information
        var username = auth.getName();
        var student = (User) userDetailsService.loadUserByUsername(username);

        // Check for self invite
        if (request.getInvited_members().stream().anyMatch(student_name -> student_name.equals(username))) {
            var res = new PostResponse("Cannot invite yourself");
            return ResponseEntity.badRequest().body(res);
        }

        // Check if user is subscribed to tournament
        var subscribed = tournament.getSubscribed_users()
                .stream()
                .anyMatch(subscriber -> subscriber.getUsername().equals(username));

        if (!subscribed) {
            var res = new PostResponse("Cannot enroll into battle without being subscribed to tournament");
            return ResponseEntity.badRequest().body(res);
        }

        if(request.getInvited_members().stream().anyMatch((s) -> s.equals(username))){
            var res = new PostResponse("Cannot invite yourself");
            return ResponseEntity.badRequest().body(res);
        }

        var battle_match = tournament.getBattles()
                .stream()
                .filter(b -> battle_title.equals(b.getTitle()))
                .findFirst();

        // Check if group can enroll
        if (battle_match.isEmpty()) {
            var res = new PostResponse("No battle found");
            return ResponseEntity.badRequest().body(res);
        }

        var battle = battle_match.get();

        //CHECK IF USERNAME IS ALREADY IN A GROUP AS A MEMBER IN THE SAME BATTLE
        for(Group g : battle.getGroups()){
            if(g.getMembers().stream().anyMatch((m) -> m.getUsername().equals(username))){
                var res = new PostResponse("you are already in a group in '"+battle_title+"' battle");
                return ResponseEntity.badRequest().body(res);
            }
        }

        // Check if battle enrollment is closed
        if (new Date(System.currentTimeMillis()).after(battle.getEnrollment_deadline())) {
            var res = new PostResponse("Enrollment period for battle closed");
            return ResponseEntity.badRequest().body(res);
        }

        // + 1 because of the creator
        if (invites.size() + 1 < battle.getMin_group_size() || invites.size() + 1 > battle.getMax_group_size()) {
            var message = "Battle group limits exceeded, group size must be between %d and %d"
                    .formatted(battle.getMin_group_size(), battle.getMax_group_size());
            var res = new PostResponse(message);
            return ResponseEntity.badRequest().body(res);
        }

        // Check if invited users are subscribed to the tournament
        for (var invitee : invites) {
            if (!tournament.getSubscribed_users().stream().anyMatch(subscriber -> subscriber.getUsername().equals(invitee.getUsername()))) {
                var res = new PostResponse("User %s is not subscribed to the tournament".formatted(invitee.getUsername()));
                return ResponseEntity.badRequest().body(res);
            }
        }

        String group_id = ObjectId.get().toString();
        String token = jwtService.generateJWT(group_id);

        // Create group
        Group group = Group.builder()
                .id(group_id)
                .leader(new GroupMember(student))
                .members(List.of(new GroupMember(student)))
                .pending_invites(invites.stream().map(PendingInvite::new).toList())
                // The repository of the group to do the download (fork)
                .repository("")
                .API_Token(token)
                .done_manual_evaluation(false)
                .total_score(0)
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
        Runnable taskSendEmail = () -> notificationService.sendSuccessfulBattleEnrollment(student, battle);
        executor.submit(taskSendEmail);

        return ResponseEntity.ok(null);
    }

    public Runnable startBattle(Tournament tournament, Battle battle) {
        List<Group> updatedGroups = getValidGroups(tournament, battle);

        return () -> {
            for (var group : updatedGroups) {
                String token = group.getAPI_Token();
                Runnable taskSendEmail = () -> notificationService.sendRepositoryInvites(group, battle, token);
                executor.submit(taskSendEmail);
            }
        };
    }

    public Runnable closeBattle(String tournamentTitle, String battleTitle){

        Tournament tournament = tournamentRepository.findTournamentByTitle(tournamentTitle);

        if(tournament == null){
            return () -> {
                throw new RuntimeException("Tournament is null");
            };
        }

        Battle battle = tournament.getBattles().stream().filter((b) -> b.getTitle().equals(battleTitle)).toList().get(0);

        List<Group> groups = battle.getGroups();

        if(battle.isManual_evaluation()){
            return () -> {
                Runnable taskSendEmail = () -> notificationService.sendManualEvaluationRequired(tournament, battleTitle);
                executor.submit(taskSendEmail);
            };
        }else {
            return () -> {
                for (var group : groups) {
                    // update the total score for ech group of the battle and the ranks
                    scoreService.updatePlayersRanks(group.getId());
                    Runnable taskSendEmail = () -> notificationService.sendNewBattleRankAvailable(group, tournamentTitle, battleTitle);
                    executor.submit(taskSendEmail);
                }
            };
        }
    }

    private List<Group> getValidGroups(Tournament tournament, Battle battle){
        List<Group> groups = battle.getGroups();
        Iterator<Group> iterator = groups.iterator();
        List<TournamentSubscriber> tournamentSubscribers = tournament.getSubscribed_users();

        while (iterator.hasNext()) {
            Group group = iterator.next();
            if (group.getMembers().size() < battle.getMin_group_size() || group.getMembers().size() > battle.getMax_group_size()) {

                Runnable taskSendEmail = () -> notificationService.sendGroupRemovedFromBattle(group, battle);
                executor.submit(taskSendEmail);

                iterator.remove();
            } else {
                group.getPending_invites().clear();

                // Check if students are both in a group and subscribed to the tournament
                List<GroupMember> member = group.getMembers();

                // work on the id
                for(GroupMember g : member){
                    String id = g.getId();
                    if(!isStudentSubscribed(tournamentSubscribers, id)){
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

        var update = new Update()
                .set("battles.$[battles].groups", groups)
                .filterArray(Criteria.where("battles._id").is(new ObjectId(battle.getId())));

        mongoTemplate.updateFirst(query, update, "tournament");

        var query2 = Query.query(
                Criteria.where("_id")
                        .is(new ObjectId(tournament.getId()))
        );

        var update2 = new Update().set("subscribed_users", tournamentSubscribers);
        mongoTemplate.updateFirst(query2, update2, "tournament");

        return groups;
    }

    private boolean isStudentSubscribed(List<TournamentSubscriber> tournamentSubscribers, String id){
        for(TournamentSubscriber t : tournamentSubscribers) {
            if (t.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<Object> getBattleInfo(String tournamentTitle, String battleTitle) {

        var battle = getBattleFromDb(tournamentTitle, battleTitle);
        if (battle == null) {
            return new ResponseEntity<>(new PostResponse("Battle not found"), HttpStatus.NOT_FOUND);
        }

        // Get data shared by educators and students
        BattleInfo battleInfo = createBattleInfo(battle);

        // Get user information
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        var username = auth.getName();

        switch (accountType) {
            case STUDENT -> {
                // Get user's group
                var userGroup = battle.getGroups()
                        .stream()
                        .filter(group -> group.getMembers()
                                .stream()
                                .anyMatch(member -> member.getUsername().equals(username)))
                        .findFirst();

                battleInfo.setGroup(userGroup);
            }
            case EDUCATOR -> battleInfo.setGroups(battle.getGroups());
        }

        return ResponseEntity.ok(battleInfo);
    }

    private BattleInfo createBattleInfo(Battle battle) {
        List<Group> groups = battle.getGroups();

        List<LeaderboardEntry> battleLeaderboard = new ArrayList<>();
        // Compute the scores of each group in the battle
        for (Group g : groups) {
            int group_score = g.getTotal_score();
            battleLeaderboard.add(new LeaderboardEntry(g.getLeader().getUsername(), group_score));
        }

        return BattleInfo.builder()
                .title(battle.getTitle())
                .description(battle.getDescription())
                .language(battle.getProject_language())
                .repository(battle.getRepository())
                .min_group_size(battle.getMin_group_size())
                .max_group_size(battle.getMax_group_size())
                .enrollment_deadline(battle.getEnrollment_deadline())
                .submission_deadline(battle.getSubmission_deadline())
                .evaluation_parameters(battle.getEvaluation_parameters())
                .manual_evaluation(battle.isManual_evaluation())
                .leaderboard(battleLeaderboard)
                .build();
    }

    public Battle getBattleFromGroupId(String groupId){
        AggregationOperation battle_match = Aggregation.match(
                Criteria.where("battles.groups._id").is(new ObjectId(groupId))
        );
        AggregationOperation unwind = Aggregation.unwind("battles");
        AggregationOperation replaceRoot = Aggregation.replaceRoot("battles");
        Aggregation aggregation = Aggregation.newAggregation(battle_match, unwind,  replaceRoot);
        AggregationResults<Battle> results = mongoTemplate.aggregate(aggregation, "tournament", Battle.class);

        return results.getUniqueMappedResult();
    }

    private Battle getBattleFromDb(String tournamentTitle, String battleTitle) {
        AggregationOperation tournament_match = Aggregation.match(
                Criteria.where("title").is(tournamentTitle)
        );
        AggregationOperation battles_unwind = Aggregation.unwind("battles");
        AggregationOperation battle_match = Aggregation.match(
                Criteria.where("battles.title").is(battleTitle)
        );
        AggregationOperation replaceRoot = Aggregation.replaceRoot("battles");

        Aggregation aggregation = Aggregation.newAggregation(tournament_match, battles_unwind, battle_match, replaceRoot);
        AggregationResults<Battle> results = mongoTemplate.aggregate(aggregation, "tournament", Battle.class);

        return results.getUniqueMappedResult();
    }
}

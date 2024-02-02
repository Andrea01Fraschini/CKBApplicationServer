package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManagersService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final UserService userService;
    private final ManagersService managersService;
    private final GroupService groupService;
    private final NotificationService notificationService;
    private final UserDetailsService userDetailsService;
    private final TournamentRepository tournamentRepository;
    private final MongoTemplate mongoTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    public ResponseEntity<PostResponse> sendManagerInvite(ManagerInviteRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if (accountType != AccountType.EDUCATOR) {
            var res = new PostResponse("Cannot send a manager invite as student");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var sender_username = auth.getName();
        var sender = (User) userDetailsService.loadUserByUsername(sender_username);
        var receiver = (User) userDetailsService.loadUserByUsername(request.getUsername());

        // Check that user is not trying to invite themselves
        if (sender.getUsername().equals(receiver.getUsername())) {
            var res = new PostResponse("You cannot invite yourself");
            return ResponseEntity.badRequest().body(res);
        }

        var tournament = tournamentRepository.findTournamentByTitle(request.getTournament_title());

        sendManagerInvite(sender, receiver, tournament);

        return ResponseEntity.ok(null);
    }

    public void sendManagerInvite(User sender, User receiver, Tournament tournament) {
        Invite invite = Invite.builder()
                .id(ObjectId.get().toString())
                .sender(sender.getUsername())
                .receiver(receiver.getUsername())
                .group_id(null)
                .tournament_id(tournament.getId()) // used to facilitate updating
                .tournament_title(tournament.getTitle()) // name and id are both unique, title is more useful for frontend
                .build();

        userService.addInvite(invite);
        managersService.inviteManager(tournament.getTitle(), receiver);

        Runnable taskSendEmail = () -> notificationService.sendInviteNotification(sender, receiver);
        executor.submit(taskSendEmail);
    }

    public ResponseEntity<PostResponse> sendGroupInvite(GroupInviteRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if (accountType != AccountType.STUDENT) {
            var res = new PostResponse("Cannot send a group invite as educator");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var sender_username = auth.getName();
        var sender = (User) userDetailsService.loadUserByUsername(sender_username);
        var receiver = (User) userDetailsService.loadUserByUsername(request.getUsername());

        // Check that user is not trying to invite themselves
        if (sender.getUsername().equals(receiver.getUsername())) {
            var res = new PostResponse("You cannot invite yourself");
            return ResponseEntity.badRequest().body(res);
        }

        var tournament = tournamentRepository.findTournamentByTitle(request.getTournament_title());
        var battle = tournament.getBattles()
                .stream()
                .filter(b -> b.getTitle().equals(request.getBattle_title()))
                .findFirst();

        if (battle.isEmpty()) {
            var res = new PostResponse("No battle found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        var context = battle.get()
                .getGroups()
                .stream()
                .filter(group -> group.getLeader().getUsername().equals(sender_username))
                .findFirst();

        if (context.isEmpty()) {
            var res = new PostResponse("No group found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        sendGroupInvite(sender, receiver, tournament, battle.get(), context.get());

        return ResponseEntity.ok(null);
    }

    public void sendGroupInvite(User sender, User receiver, Tournament tournament, Battle battle, Group group) {
        Invite invite = Invite.builder()
                .id(ObjectId.get().toString())
                .sender(sender.getUsername())
                .receiver(receiver.getUsername())
                .group_id(group.getId())
                .tournament_id(tournament.getId()) // used to facilitate updating
                .tournament_title(tournament.getTitle()) // name and id are both unique, title is more useful for frontend
                .battle_title(battle.getTitle())
                .build();

        userService.addInvite(invite);
        groupService.inviteStudent(tournament.getTitle(), battle.getTitle(), group.getId(), receiver);

        Runnable taskSendEmail = () -> notificationService.sendInviteNotification(sender, receiver);
        executor.submit(taskSendEmail);
    }

    public ResponseEntity<PostResponse> updateInviteStatus(InviteStatusUpdateRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var username = auth.getName();
        var user = (User) userDetailsService.loadUserByUsername(username);
        var invite_id = request.getInvite_id();
        var accepted = request.isAccepted();

        var invite = user.getInvites()
                .stream()
                .filter(inv -> invite_id.equals(inv.getId()))
                .findFirst();

        if (invite.isEmpty()) {
            var res = new PostResponse("No invite found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }

        // Remove invite from user
        var update = new Update().pull("invites", Query.query(Criteria.where("_id").is(new ObjectId(invite_id))));
        mongoTemplate.updateFirst(Query.query(Criteria.where("username").is(username)), update, "user");

        if (user.getAccountType() == AccountType.STUDENT) {
            updateGroupInviteStatus(invite.get(), user, accepted);
        } else {
            updateManagerInviteStatus(invite.get(), user, accepted);
        }

        var sender = (User) userDetailsService.loadUserByUsername(invite.get().getSender());

        Runnable taskSendEmail = () -> notificationService.sendInviteStatusUpdate(sender, accepted);
        executor.submit(taskSendEmail);

        return ResponseEntity.ok(null);
    }

    public void updateGroupInviteStatus(Invite invite, User user, boolean accepted) {
        // Update pending invites in tournament
        if (accepted) {
            groupService.acceptGroupInvite(invite.getTournament_id(), invite.getGroup_id(), user);
        } else {
            groupService.rejectGroupInvite(invite.getTournament_id(), invite.getGroup_id(), user);
        }
    }

    public void updateManagerInviteStatus(Invite invite, User user, boolean accepted) {
        // Update pending invites in tournament
        if (accepted) {
            managersService.acceptManagerInvite(invite.getTournament_id(), user);
        } else {
            managersService.rejectManagerInvite(invite.getTournament_id(), user);
        }
    }

    public ResponseEntity<List<InviteController.InviteCard>> getUserInviteCards() {
        // Get user data
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (User) userDetailsService.loadUserByUsername(auth.getName());

        var invites = user.getInvites();

        return ResponseEntity.ok(
                invites.stream()
                        .map(invite -> new InviteController.InviteCard(
                                invite.getId(),
                                invite.getSender(),
                                invite.getTournament_id(),
                                invite.getTournament_title(),
                                invite.getBattle_title()
                        ))
                        .toList()
        );
    }

}

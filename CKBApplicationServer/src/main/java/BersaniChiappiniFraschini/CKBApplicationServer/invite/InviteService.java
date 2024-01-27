package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupService;
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

@Service
@RequiredArgsConstructor
public class InviteService {
    private final UserService userService;
    private final TournamentService tournamentService;
    private final GroupService groupService;
    private final NotificationService notificationService;
    private final UserDetailsService userDetailsService;
    private final TournamentRepository tournamentRepository;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<PostResponse> sendManagerInvite(User sender, User receiver, Tournament context) {
        Invite invite = Invite.builder()
                .id(ObjectId.get().toString())
                .sender(sender.getUsername())
                .receiver(receiver.getUsername())
                .tournament_id(context.getId())
                .build();

        userService.addInvite(invite);
        tournamentService.inviteManager(context, receiver);
        notificationService.sendInviteNotification(sender, receiver);

        return ResponseEntity.ok(null);
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

        Invite invite = Invite.builder()
                .id(ObjectId.get().toString())
                .sender(sender.getUsername())
                .receiver(receiver.getUsername())
                .group_id(context.get().getId())
                .tournament_id(tournament.getId()) // used to facilitate updating
                .build();

        userService.addInvite(invite);
        groupService.inviteStudent(request.getTournament_title(), request.getBattle_title(), context.get().getId(), receiver);
        notificationService.sendInviteNotification(sender, receiver);

        return ResponseEntity.ok(null);
    }

    public ResponseEntity<PostResponse> updateGroupInviteStatus(InviteStatusUpdateRequest request) {
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

        // Update pending invites in tournament
        if (accepted) {
            groupService.acceptGroupInvite(invite.get().getTournament_id(), invite.get().getGroup_id(), user);
        } else {
            groupService.rejectGroupInvite(invite.get().getTournament_id(), invite.get().getGroup_id(), user);
        }

        var sender = (User) userDetailsService.loadUserByUsername(invite.get().getSender());
        notificationService.sendInviteStatusUpdate(sender, accepted);

        return ResponseEntity.ok(null);
    }
}

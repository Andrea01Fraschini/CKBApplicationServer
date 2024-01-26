package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import lombok.RequiredArgsConstructor;
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

    public void sendManagerInvite(User sender, User receiver, Tournament context) {
        Invite invite = Invite.builder()
                .sender(sender)
                .receiver(receiver)
                .tournament(context)
                .group(null)
                .build();

        userService.addInvite(invite);
        tournamentService.inviteManager(context, receiver);
        notificationService.sendInviteNotification(invite);
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
                .sender(sender)
                .receiver(receiver)
                .tournament(null)
                .group(context.get())
                .build();

        userService.addInvite(invite);
        groupService.inviteStudent(tournament.getId(), battle.get().getId(), context.get().getId(), receiver);
        notificationService.sendInviteNotification(invite);

        return ResponseEntity.ok(null);
    }
}

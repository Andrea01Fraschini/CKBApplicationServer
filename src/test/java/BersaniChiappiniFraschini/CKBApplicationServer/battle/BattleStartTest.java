package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.*;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class BattleStartTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private ScoreService scoreService;
    @Mock
    private TournamentService tournamentService;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private GitHubManagerService gitHubManagerService;
    @InjectMocks
    private BattleService battleService;

    private static Battle battle;
    private static Tournament tournament;

    @BeforeEach
    public void setup() {
        var invites = new ArrayList<PendingInvite>();
        invites.add(new PendingInvite());

        var user_a = User.builder()
                .id("BCD918212315CD42BEEE63F3")
                .username("I'm a subscriber")
                .build();

        var user_b = User.builder()
                .id("EEEEABCDABCDABCD12345600")
                .username("I'm already subscribed")
                .build();

        List<Group> groups = List.of(
                Group.builder()
                        .pending_invites(invites)
                        .members(List.of(
                                new GroupMember(user_a),
                                new GroupMember(user_b)
                        )).build());

        battle = Battle.builder()
                .id("AAAAAA0123451989EEEEEE01")
                .enrollment_deadline(new Date(System.currentTimeMillis()+1000*3600))
                .groups(groups)
                .min_group_size(1)
                .max_group_size(3)
                .title("Battle title")
                .build();

        var subs = new ArrayList<TournamentSubscriber>();
        subs.add(new TournamentSubscriber(user_a));
        subs.add(new TournamentSubscriber(user_b));

        tournament = Tournament.builder()
                .id("FFFFFF0123451989BBBBBB99")
                .title("Tournament Title")
                .subscribed_users(subs)
                .educators(List.of(new TournamentManager(User.builder()
                        .username("Tyler the creator")
                        .build())))
                .battles(List.of(battle)).build();

        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(tournament);

        when(gitHubManagerService.saveFileAndCreateRepository(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture("repo"));
    }

    @Test
    public void shouldStartBattle(){
        battleService.startBattle(tournament, battle).run();
    }
}
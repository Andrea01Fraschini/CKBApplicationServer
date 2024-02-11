package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.scores.ScoreService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.*;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class BattleCloseTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private ScoreService scoreService;
    @Mock
    private TournamentService tournamentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private GitHubManagerService gitHubManagerService;
    @InjectMocks
    private BattleService battleService;


    @Test
    public void shouldCloseBattleWithManualEvaluation(){
        setupBattleWithManualEvaluation(true);
        battleService.closeBattle("Tournament Title", "Battle title").run();
    }

    @Test
    public void shouldCloseBattleWithoutManualEvaluation(){
        setupBattleWithManualEvaluation(false);
        battleService.closeBattle("Tournament Title", "Battle title").run();
    }

    @Test
    public void shouldNotFindTournament(){
        setupBattleWithManualEvaluation(false);
        assertThrows(RuntimeException.class, () ->
                battleService.closeBattle("Wrong title", "Battle title").run());
    }


    private void setupBattleWithManualEvaluation(boolean manual_evaluation) {
        List<Group> groups = List.of(
                Group.builder()
                        .members(List.of(
                                new GroupMember(User.builder().username("I'm a subscriber").build()),
                                new GroupMember(User.builder().username("I'm already subscribed").build())
                        )).build());

        when(tournamentRepository.findTournamentByTitle("Tournament Title"))
                .thenReturn(Tournament.builder()
                        .id("FFFFFF0123451989BBBBBB99")
                        .title("Tournament Title")
                        .subscribed_users(List.of(
                                new TournamentSubscriber(
                                        User.builder()
                                                .username("I'm a subscriber")
                                                .build()
                                ),
                                new TournamentSubscriber(
                                        User.builder()
                                                .username("I'm already subscribed")
                                                .build()
                                )
                        ))
                        .educators(List.of(new TournamentManager(User.builder()
                                .username("Tyler the creator")
                                .build())))
                        .battles(List.of(Battle.builder()
                                .id("AAAAAA0123451989EEEEEE01")
                                .enrollment_deadline(new Date(System.currentTimeMillis()+1000*3600))
                                .groups(groups)
                                .manual_evaluation(manual_evaluation)
                                .min_group_size(1)
                                .max_group_size(3)
                                .title("Battle title")
                                .build()))
                        .build());

        when(gitHubManagerService.saveFileAndCreateRepository(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture("repo"));
    }
}
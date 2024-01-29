package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class BattleServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private TournamentService tournamentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private InviteService inviteService;
    @Mock
    private EventService eventService;
    @InjectMocks
    private BattleService battleService;
    @Test
    @WithMockUser(username = "Tyler the creator", authorities = { "EDUCATOR" })
    public void shouldCreateBattle(){
        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .battles(List.of(Battle.builder()
                                .title("Different battle title")
                                .build()))
                        .build());

        BattleCreationRequest request = new BattleCreationRequest(
                "Tournament title",
                "Battle title",
                1,
                4,
                "The final showdown of ultimate destiny",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                new Date(System.currentTimeMillis()+1000*60*60*24*5),
                false,
                List.of()
        );

        var response = battleService.createBattle(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "Tyler the creator", authorities = { "EDUCATOR" })
    public void shouldNotCreateDuplicateBattle(){
        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .battles(List.of(Battle.builder()
                                .title("Battle title")
                                .build()))
                        .build());

        BattleCreationRequest request = new BattleCreationRequest(
                "Tournament title",
                "Battle title",
                1,
                4,
                "The final showdown of ultimate destiny",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                new Date(System.currentTimeMillis()+1000*60*60*24*5),
                false,
                List.of()
        );

        var response = battleService.createBattle(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "Tyler the creator", authorities = { "STUDENT" })
    public void shouldNotCreateBattleAsStudent(){
        var response = battleService.createBattle(new BattleCreationRequest());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
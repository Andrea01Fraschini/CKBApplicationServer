package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.event.EventService;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.*;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class BattleCreationAndEnrollmentTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private TournamentService tournamentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private GitHubManagerService gitHubManagerService;
    @Mock
    private InviteService inviteService;
    @Mock
    private EventService eventService;
    @InjectMocks
    private BattleService battleService;

    @BeforeEach
    public void setup(){

        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(User.builder()
                        .id("FFFFFF0123451989BBBBBB99")
                        .username(SecurityContextHolder.getContext().getAuthentication().getName())
                        .build());

        when(authenticationService.generateToken(anyString()))
                .thenReturn("myb34ut1fult0k3n");

        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("FFFFFF0123451989BBBBBB99")
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
                                .groups(List.of(Group.builder()
                                        .members(List.of(new GroupMember(User.builder()
                                                .username("I'm already subscribed")
                                                .build())))
                                        .build()))
                                .min_group_size(1)
                                .max_group_size(3)
                                .title("Battle title")
                                .build()))
                        .build());


    }

    @Test
    @WithMockUser(username = "Tyler the creator", authorities = { "EDUCATOR" })
    public void shouldCreateBattle() {


        BattleCreationRequest request = new BattleCreationRequest(
                "Tournament title",
                "New Battle title",
                1,
                4,
                "The final showdown of ultimate destiny",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                new Date(System.currentTimeMillis()+1000*60*60*24*5),
                false,
                List.of(),
                "java",
                "test_file_name",
                null
        );

        var response = battleService.createBattle(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "Tyler the creator", authorities = { "EDUCATOR" })
    public void shouldNotCreateDuplicateBattle() {

        BattleCreationRequest request = new BattleCreationRequest(
                "Tournament title",
                "Battle title",
                1,
                4,
                "The final showdown of ultimate destiny",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                new Date(System.currentTimeMillis()+1000*60*60*24*5),
                false,
                List.of(),
                "java",
                "test_file_name".toLowerCase(),
                null
        );

        var response = battleService.createBattle(request);
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals("Battle with title Battle title already exists", error_msg)
        );
    }

    @Test
    @WithMockUser(username = "I'm not a manager of the tournament", authorities = { "EDUCATOR" })
    public void shouldNotCreateBattleIfNotAManager()  {
        BattleCreationRequest request = new BattleCreationRequest(
                "Tournament title",
                "New Battle title",
                1,
                4,
                "The final showdown of ultimate destiny",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                new Date(System.currentTimeMillis()+1000*60*60*24*5),
                false,
                List.of(),
                "java",
                "test_file_name",
                null
        );


        var response = battleService.createBattle(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "I'm not an educator", authorities = { "STUDENT" })
    public void shouldNotCreateBattleAsStudent(){

        var response = battleService.createBattle(new BattleCreationRequest());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "I'm a subscriber", authorities = { "STUDENT" })
    public void shouldEnrollGroup(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "I'm not a student", authorities = {"EDUCATOR"})
    public void shouldNotEnrollGroupAsEducator(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "I'm a subscriber", authorities = {"STUDENT"})
    public void shouldNotEnrollIfNonExistentBattle(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Wrong Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals("No battle found", error_msg)
        );

    }

    @Test
    @WithMockUser(username = "I'm a subscriber", authorities = {"STUDENT"})
    public void shouldNotEnrollAfterSubscriptionDeadline(){
        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("FFFFFF0123451989BBBBBB99")
                        .subscribed_users(List.of(new TournamentSubscriber(
                                User.builder()
                                        .username("I'm a subscriber")
                                        .build()
                        )))
                        .educators(List.of(new TournamentManager(User.builder()
                                .username("Tyler the creator")
                                .build())))
                        .battles(List.of(Battle.builder()
                                .id("AAAAAA0123451989EEEEEE01")
                                .enrollment_deadline(new Date(System.currentTimeMillis()-1000*25))
                                .groups(List.of())
                                .min_group_size(1)
                                .max_group_size(3)
                                .title("Battle title")
                                .build()))
                        .subscribed_users(List.of(TournamentSubscriber.builder()
                                .username("I'm a student")
                                .build(), TournamentSubscriber.builder()
                                .username("I'm a subscriber")
                                .build()))
                        .build());

        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals("Enrollment period for battle closed", error_msg)
        );
    }

    @Test
    @WithMockUser(username = "I'm a subscriber", authorities = {"STUDENT"})
    public void shouldNotEnrollWhenWrongGroupSize(){
        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("FFFFFF0123451989BBBBBB99")
                        .subscribed_users(List.of(new TournamentSubscriber(
                                User.builder()
                                        .username("I'm a subscriber")
                                        .build()
                        )))
                        .educators(List.of(new TournamentManager(User.builder()
                                .username("Tyler the creator")
                                .build())))
                        .battles(List.of(Battle.builder()
                                .id("AAAAAA0123451989EEEEEE01")
                                .enrollment_deadline(new Date(System.currentTimeMillis()+1000*3600))
                                .groups(List.of())
                                .min_group_size(1)
                                .max_group_size(2)
                                .title("Battle title")
                                .build()))
                        .subscribed_users(List.of(TournamentSubscriber.builder()
                                .username("I'm a student")
                                .build(), TournamentSubscriber.builder()
                                .username("I'm a subscriber")
                                .build()))
                        .build());

        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);
        var expected_error_msg = "Battle group limits exceeded, group size must be between 1 and 2";
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals(expected_error_msg, error_msg)
        );
    }

    @Test
    @WithMockUser(username = "I'm NOT a subscriber", authorities = { "STUDENT" })
    public void shouldNotEnrollIfNotSubscribedToTournament(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);
        var expected_error_msg = "Cannot enroll into battle without being subscribed to tournament";
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals(expected_error_msg, error_msg)
        );
    }

    @Test
    @WithMockUser(username = "I'm a subscriber", authorities = { "STUDENT" })
    public void shouldNotEnrollIfSelfInvited(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "I'm a subscriber")
        );

        var response = battleService.enrollGroup(request);
        var expected_error_msg = "Cannot invite yourself";
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals(expected_error_msg, error_msg)
        );
    }

    @Test
    @WithMockUser(username = "I'm already subscribed", authorities = { "STUDENT" })
    public void shouldNotEnrollIfAlreadyInAGroup(){
        BattleEnrollmentRequest request = new BattleEnrollmentRequest(
                "Tournament title",
                "Battle title",
                List.of("My friend", "My other friend")
        );

        var response = battleService.enrollGroup(request);
        var expected_error_msg = "you are already in a group in 'Battle title' battle";
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()-> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
                ()-> assertEquals(expected_error_msg, error_msg)
        );
    }

}
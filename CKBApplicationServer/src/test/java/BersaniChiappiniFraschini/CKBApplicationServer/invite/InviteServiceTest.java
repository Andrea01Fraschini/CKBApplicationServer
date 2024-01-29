package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManagersService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
class InviteServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private NotificationService notificationService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private UserService userService;
    @Mock
    private GroupService groupService;
    @Mock
    private ManagersService managersService;

    @InjectMocks
    private InviteService inviteService;
    @BeforeEach
    public void setup(){
        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder().id("idTest").build());
    }
    @Test
    @WithMockUser(username = "TestUser", authorities = {"EDUCATOR"})
    public void shouldSendManagerInvite(){
        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder().username("TestUser").build())
                .thenReturn(User.builder().username("OtherTestUser").build());

        ManagerInviteRequest request = new ManagerInviteRequest(
                "Test tournament",
                "OtherTestUser"
        );

        var response = inviteService.sendManagerInvite(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldNotSendManagerInviteAsStudent(){
        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder().username("TestUser").build())
                .thenReturn(User.builder().username("OtherTestUser").build());

        ManagerInviteRequest request = new ManagerInviteRequest(
                "Test tournament",
                "OtherTestUser"
        );

        var response = inviteService.sendManagerInvite(request);

        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()->assertEquals("Cannot send a manager invite as student", error_msg),
                ()->assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
        );
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldSendGroupInvite(){
        var sender = User.builder().username("TestUser").build();
        var battle = Battle.builder()
                .title("Test Battle")
                .groups(List.of(Group.builder().leader(new GroupMember(sender)).build()))
                .build();

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(sender)
                .thenReturn(User.builder().username("OtherTestUser").build());

        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("idTest")
                        .battles(List.of(battle))
                        .build());

        var request = new GroupInviteRequest(
                "Test tournament",
                "Test Battle",
                "OtherTestUser"
        );

        var response = inviteService.sendGroupInvite(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"EDUCATOR"})
    public void shouldNotSendGroupInviteAsEducator(){

        var request = new GroupInviteRequest(
                "Test tournament",
                "Test Battle",
                "OtherTestUser"
        );

        var response = inviteService.sendGroupInvite(request);

        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()->assertEquals("Cannot send a group invite as educator", error_msg),
                ()->assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode())
        );
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldNotFindBattle(){
        var sender = User.builder().username("TestUser").build();
        var battle = Battle.builder()
                .title("Wrong Battle")
                .groups(List.of(Group.builder().leader(new GroupMember(sender)).build()))
                .build();

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(sender)
                .thenReturn(User.builder().username("OtherTestUser").build());

        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("idTest")
                        .battles(List.of(battle))
                        .build());

        var request = new GroupInviteRequest(
                "Test tournament",
                "Test Battle",
                "OtherTestUser"
        );

        var response = inviteService.sendGroupInvite(request);

        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()->assertEquals("No battle found", error_msg),
                ()->assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldNotFindGroup(){
        var sender = User.builder().username("TestUser").build();
        var differentLeader = User.builder().username("DifferentLeader").build();
        var battle = Battle.builder()
                .title("Test Battle")
                .groups(List.of(Group.builder().leader(new GroupMember(differentLeader)).build()))
                .build();

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(sender)
                .thenReturn(User.builder().username("OtherTestUser").build());

        when(tournamentRepository.findTournamentByTitle(anyString()))
                .thenReturn(Tournament.builder()
                        .id("idTest")
                        .battles(List.of(battle))
                        .build());

        var request = new GroupInviteRequest(
                "Test tournament",
                "Test Battle",
                "OtherTestUser"
        );

        var response = inviteService.sendGroupInvite(request);

        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()->assertEquals("No group found", error_msg),
                ()->assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldUpdateGroupInviteStatus(){
        InviteStatusUpdateRequest request = new InviteStatusUpdateRequest(
                "ABCDEF0123456789EBAFDAFE", "tournament_id", true
        );

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder()
                        .username("TestUser")
                        .accountType(AccountType.STUDENT)
                        .invites(List.of(Invite.builder()
                                .id("ABCDEF0123456789EBAFDAFE")
                                .tournament_id("tournament_id").build()))
                        .build());

        var response = inviteService.updateInviteStatus(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"EDUCATOR"})
    public void shouldUpdateManagerInviteStatus(){
        InviteStatusUpdateRequest request = new InviteStatusUpdateRequest(
                "ABCDEF0123456789EBAFDAFE", "tournament_id", true
        );

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder()
                        .username("TestUser")
                        .accountType(AccountType.EDUCATOR)
                        .invites(List.of(Invite.builder()
                                .id("ABCDEF0123456789EBAFDAFE")
                                .tournament_id("tournament_id").build()))
                        .build());

        var response = inviteService.updateInviteStatus(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


    @Test
    @WithMockUser(username = "TestUser", authorities = {"STUDENT"})
    public void shouldNotFindInvites(){
        InviteStatusUpdateRequest request = new InviteStatusUpdateRequest(
                "ABCDEF0123456789EBAFDAFE", "tournament_id", true
        );

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder()
                        .username("TestUser")
                        .accountType(AccountType.STUDENT)
                        .invites(List.of())
                        .build());

        var response = inviteService.updateInviteStatus(request);
        var error_msg = Objects.requireNonNull(response.getBody()).getError_msg();
        assertAll(
                ()->assertEquals("No invite found", error_msg),
                ()->assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode())
        );
    }
}
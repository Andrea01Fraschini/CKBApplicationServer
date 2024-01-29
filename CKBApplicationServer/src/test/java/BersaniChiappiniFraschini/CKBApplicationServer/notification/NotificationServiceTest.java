package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentManager;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentSubscriber;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class NotificationServiceTest {
    @Mock
    private JavaMailSender emailSender;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;


    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setup(){
        //Instead of sending an email, print to console

        System.setOut(new PrintStream(outputStreamCaptor));

        doAnswer(i->{
            SimpleMailMessage email = (SimpleMailMessage) Arrays.stream(i.getArguments()).findFirst().get();
            System.out.println(email.getText());
            return null;
        }).when(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @WithMockUser(username = "TestUser", authorities = {"EDUCATOR"})
    public void shouldNotifyTournamentCreation(){
        when(userRepository.getAllStudentsEmails())
                .thenReturn(List.of(User.builder()
                        .email("test@email.com")
                        .build()));


        Tournament tournament = Tournament.builder()
                .title("Test tournament")
                .educators(List.of(
                        new TournamentManager(
                                "Creator_id",
                                "TournamentCreator"
                        )))
                .build();

        notificationService.sendTournamentCreationNotifications(tournament);

        String notificationHeader = "You received a notification from code kata battle:\n\n'%s'";
        String templateMsg = notificationHeader.formatted("A tournament titled '%s' has been created by '%s'. Go ahead and join it!");
        String templateEmail = "%s\n\n-CodeKataBattle notification service";
        String expectedEmail = templateEmail.formatted(
                templateMsg.formatted("Test tournament", "TournamentCreator"));

        assertEquals(expectedEmail.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void shouldNotifyBattleCreation(){
        Battle battle = Battle.builder()
                .title("Test battle")
                .build();
        Tournament tournament = Tournament.builder()
                .title("Test tournament")
                .subscribed_users(List.of(
                        new TournamentSubscriber(User.builder().email("test@email.mock").build())
                ))
                .build();

        notificationService.sendBattleCreationNotification(battle, tournament);

        String notificationHeader = "You received a notification from code kata battle:\n\n'%s'";
        String templateMsg = notificationHeader.formatted("A battle titled '%s' has been created in tournament '%s'.");
        String templateEmail = "%s\n\n-CodeKataBattle notification service";
        String expectedEmail = templateEmail.formatted(
                templateMsg.formatted("Test battle", "Test tournament"));

        assertEquals(expectedEmail.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void shouldNotifyInvite(){
        var sender = User.builder().username("Test sender").build();
        var receiver = User.builder().email("receiver@email.mock").build();

        notificationService.sendInviteNotification(sender, receiver);

        String notificationHeader = "You received a notification from code kata battle:\n\n'%s'";
        String templateMsg = notificationHeader.formatted("You received an invite from %s");
        String templateEmail = "%s\n\n-CodeKataBattle notification service";
        String expectedEmail = templateEmail.formatted(
                templateMsg.formatted("Test sender"));

        assertEquals(expectedEmail.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void shouldNotifyResponseToInvite(){
        var sender = User.builder().username("Test sender").build();

        notificationService.sendInviteStatusUpdate(sender, true);

        String notificationHeader = "You received a notification from code kata battle:\n\n'%s'";
        String templateMsg = notificationHeader.formatted("%s has %s your invite");
        String templateEmail = "%s\n\n-CodeKataBattle notification service";
        String expectedEmail = templateEmail.formatted(
                templateMsg.formatted("Test sender", "accepted"));

        assertEquals(expectedEmail.trim(), outputStreamCaptor.toString().trim());
    }

    @Test
    public void shouldNotifyBattleStart(){
        var member = User.builder().email("test@email.mock").build();
        var battle = Battle.builder()
                .title("Test battle")
                .repository("github.com")
                .build();
        var group = Group.builder()
                .members(List.of(new GroupMember(member)))
                .build();

        notificationService.sendRepositoryInvites(group, battle, "api_token");

        String notificationHeader = "You received a notification from code kata battle:\n\n'%s'";
        String templateMsg = notificationHeader.formatted("The battle '%s' has started! You can find the repository at the following link: %s. Remember to include your group access token: %s");
        String templateEmail = "%s\n\n-CodeKataBattle notification service";
        String expectedEmail = templateEmail.formatted(
                templateMsg.formatted("Test battle", "github.com", "api_token"));

        assertEquals(expectedEmail.trim(), outputStreamCaptor.toString().trim());
    }
}
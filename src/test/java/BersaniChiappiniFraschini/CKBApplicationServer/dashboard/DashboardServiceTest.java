package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.BattleService;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.PushActionService;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.InviteService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.Notification;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationType;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentManager;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentSubscriber;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DataMongoTest(includeFilters = @ComponentScan.Filter(Service.class))
class DashboardServiceTest {
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private BattleService battleService;
    @MockBean
    private PushActionService pushActionService;
    @MockBean
    private InviteService inviteService;
    @MockBean
    private NotificationService notificationService;

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private DashboardService dashboardService;

    @BeforeEach
    void setup(){
        var notifications = List.of(Notification.builder()
                        .is_closed(true)
                        .message("I've been closed!")
                        .build(),
                Notification.builder()
                        .is_closed(false)
                        .id("FF51BBFBB1BB99551BB1BB99")
                        .creation_date(parseDate("2024-12-20"))
                        .type(NotificationType.NEW_TOURNAMENT)
                        .message("Hot new tournament in your area")
                        .build()
        );

        var manager = User.builder()
                .id("AA1AA9AAA5A939A33AAA3389")
                .username("I'm the manager")
                .notifications(notifications)
                .build();

        var user = User.builder()
                .username("I'm subscribed")
                .notifications(notifications)
                .build();

        when(userDetailsService.loadUserByUsername("I'm subscribed"))
                .thenReturn(user);

        when(userDetailsService.loadUserByUsername("I'm the manager"))
                .thenReturn(manager);

        var buddy = User.builder()
                .username("I'm a friend!")
                .build();

        Tournament testingTournament = Tournament.builder()
                .id("FFFFFF0123451989BBBBBB99")
                .title("Tournament title")
                .is_open(true)
                .educators(List.of(new TournamentManager(manager)))
                .subscribed_users(List.of(new TournamentSubscriber(user)))
                .subscription_deadline(parseDate("2024-12-8"))
                .battles(List.of(Battle.builder()
                        .title("Battle title")
                        .submission_deadline(parseDate("2024-12-25"))
                        .groups(List.of(Group.builder()
                                .id("ABCDEF0123456789ABBEDD01")
                                .total_score(69)
                                .leader(new GroupMember(user))
                                .API_Token("v3r1n1c3t0k3n")
                                .done_manual_evaluation(false)
                                .last_update(parseDate("2024-12-20"))
                                .members(List.of(new GroupMember(user), new GroupMember(buddy)))
                                .build()))
                        .build()))
                .build();

        mongoTemplate.remove(new Query(), "tournament");
        mongoTemplate.insert(testingTournament, "tournament");
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    @Test
    @WithMockUser(username = "I'm subscribed", authorities = {"STUDENT"})
    public void shouldGetCorrectStudentDashboardAsStudent(){
        var response = dashboardService.getDashboard();
        var notifications = response.getNotifications();
        var cards = response.getCards();

        assertTrue(cards.get(0) instanceof CardInfoStudent);

        CardInfoStudent info = (CardInfoStudent) cards.get(0);

        assertAll(
                ()->assertEquals("STUDENT", response.getAccount_type()),
                ()->assertEquals("Tournament title", info.tournament_title()),
                ()->assertEquals("Battle title", info.battle_title()),
                ()->assertEquals(69, info.current_group_score()),
                ()->assertEquals(parseDate("2024-12-20"), info.last_update()),
                ()->assertEquals(parseDate("2024-12-25"), info.submission_deadline()),
                ()->assertEquals("I'm subscribed", info.students().get(0).username()),
                ()->assertEquals("v3r1n1c3t0k3n", info.API_Token()),

                ()-> assertEquals(1, notifications.size()),
                ()->assertEquals("Hot new tournament in your area", notifications.get(0).getMessage()),
                ()->assertEquals(NotificationType.NEW_TOURNAMENT, notifications.get(0).getType()),
                ()->assertEquals("FF51BBFBB1BB99551BB1BB99", notifications.get(0).getId())
        );
    }

    @Test
    @WithMockUser(username = "I'm the manager", authorities = {"EDUCATOR"})
    public void shouldGetCorrectEducatorDashboardAsEducator(){
        var response = dashboardService.getDashboard();
        var notifications = response.getNotifications();
        var cards = response.getCards();

        assertTrue(cards.get(0) instanceof CardInfoEducator);

        CardInfoEducator info = (CardInfoEducator) cards.get(0);

        assertAll(
                ()->assertEquals("EDUCATOR", response.getAccount_type()),
                ()->assertEquals("Tournament title", info.tournament_title()),
                ()->assertEquals(1, info.subscribed_students_count()),
                ()->assertEquals(1, info.number_of_battles()),
                ()->assertEquals(parseDate("2024-12-8"), info.subscription_deadline()),
                ()-> assertTrue(info.is_open()),
                ()->assertEquals("I'm the manager", info.educators().get(0).username()),

                ()-> assertEquals(1, notifications.size()),
                ()->assertEquals("Hot new tournament in your area", notifications.get(0).getMessage()),
                ()->assertEquals(NotificationType.NEW_TOURNAMENT, notifications.get(0).getType()),
                ()->assertEquals("FF51BBFBB1BB99551BB1BB99", notifications.get(0).getId())
        );
    }
}
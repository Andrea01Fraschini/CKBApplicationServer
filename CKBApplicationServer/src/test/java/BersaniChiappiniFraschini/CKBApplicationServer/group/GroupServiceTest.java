package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;


@DataMongoTest(includeFilters = @ComponentScan.Filter(Service.class))
class GroupServiceTest {
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JavaMailSender javaMailSender;
    @MockBean
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GroupService groupService;

    @BeforeEach
    void setup(){
        Tournament testingTournament = Tournament.builder()
                .id("FFFFFF0123451989BBBBBB99")
                .title("Tournament title")
                .battles(List.of(Battle.builder()
                        .title("Battle title")
                        .groups(List.of(Group.builder()
                                .id("ABCDEF0123456789ABBEDD01")
                                .build()))
                        .build()))
                .build();
        mongoTemplate.remove(new Query(), "tournament");
        mongoTemplate.insert(testingTournament, "tournament");
    }

    @Test
    public void shouldInviteUser(){
        var receiver = User.builder()
                .id("EEEEEE0123999900EEEEEE88")
                .build();

        groupService.inviteStudent(
                "Tournament title",
                "Battle title",
                "ABCDEF0123456789ABBEDD01",
                receiver);

        Tournament updatedTournament = mongoTemplate.findOne(new Query(), Tournament.class, "tournament");

        var invites = updatedTournament.getBattles().get(0).getGroups().get(0).getPending_invites();

        assertAll(
                ()->assertEquals(1, invites.size()),
                ()->assertEquals("EEEEEE0123999900EEEEEE88".toLowerCase(), invites.get(0).getId())
        );
    }

    @Test
    public void shouldAddInvitedUserIfTheyAccepted(){
        var newUser = User.builder()
                .id("EEEEEE0123999900EEEEEE88")
                .username("Invited user")
                .email("mail@test.mock")
                .build();

        groupService.inviteStudent(
                "Tournament title",
                "Battle title",
                "ABCDEF0123456789ABBEDD01",
                newUser);

        groupService.acceptGroupInvite(
                "FFFFFF0123451989BBBBBB99",
                "ABCDEF0123456789ABBEDD01",
                newUser);

        Tournament updatedTournament = mongoTemplate.findOne(new Query(), Tournament.class, "tournament");

        var group = updatedTournament.getBattles().get(0).getGroups().get(0);
        var invites = group.getPending_invites();
        var member_username = group.getMembers().get(0).getUsername();

        assertAll(
                ()->assertEquals(0, invites.size()),
                ()->assertEquals("Invited user", member_username)
        );
    }

    @Test
    public void shouldNotAddInvitedUserIfTheyRejected(){
        var newUser = User.builder()
                .id("EEEEEE0123999900EEEEEE88")
                .username("Invited user")
                .email("mail@test.mock")
                .build();

        groupService.inviteStudent(
                "Tournament title",
                "Battle title",
                "ABCDEF0123456789ABBEDD01",
                newUser);

        groupService.rejectGroupInvite(
                "FFFFFF0123451989BBBBBB99",
                "ABCDEF0123456789ABBEDD01",
                newUser);

        Tournament updatedTournament = mongoTemplate.findOne(new Query(), Tournament.class, "tournament");

        var group = updatedTournament.getBattles().get(0).getGroups().get(0);
        var invites = group.getPending_invites();
        var members = group.getMembers();

        assertAll(
                ()->assertEquals(0, invites.size()),
                ()->assertNull(members)
        );
    }
}
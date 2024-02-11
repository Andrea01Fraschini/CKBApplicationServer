package BersaniChiappiniFraschini.CKBApplicationServer.scores;

import BersaniChiappiniFraschini.CKBApplicationServer.analysis.EvaluationResult;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.group.GroupMember;
import BersaniChiappiniFraschini.CKBApplicationServer.group.ManualEvaluationRequest;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.TestStatus;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentManager;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@DataMongoTest(includeFilters = @ComponentScan.Filter(Service.class))
class ScoreServiceTest {

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup(){
        var manager = User.builder()
                .id("AA1AA9AAA5A939A33AAA3389")
                .username("I'm the one who knocks")
                .build();

        when(userDetailsService.loadUserByUsername(anyString()))
                .thenReturn(manager);

        var user = User.builder()
                .username("I'm a sub")
                .build();

        Tournament testingTournament = Tournament.builder()
                .id("FFFFFF0123451989BBBBBB99")
                .title("Tournament title")
                .educators(List.of(new TournamentManager(manager)))
                .subscribed_users(List.of(new TournamentSubscriber(user)))
                .battles(List.of(Battle.builder()
                        .title("Battle title")
                        .manual_evaluation(true)
                        .groups(List.of(Group.builder()
                                .id("ABCDEF0123456789ABBEDD01")
                                .done_manual_evaluation(false)
                                .members(List.of(new GroupMember(user)))
                                .build()))
                        .build()))
                .build();
        mongoTemplate.remove(new Query(), "tournament");
        mongoTemplate.insert(testingTournament, "tournament");
    }

    @Test
    @WithMockUser(username = "I'm the one who knocks", authorities = {"EDUCATOR"})
    public void shouldSetManualScore(){
        ManualEvaluationRequest request = new ManualEvaluationRequest();
        request.setTournament_title("Tournament title");
        request.setBattle_title("Battle title");
        request.setGroup_id("ABCDEF0123456789ABBEDD01".toLowerCase());
        request.setPoints(50);

        var response = scoreService.setManualScores(request);

        var tournament = mongoTemplate.findById("FFFFFF0123451989BBBBBB99", Tournament.class);
        assertNotNull(tournament);

        var group = tournament.getBattles().get(0).getGroups().get(0);
        var manualAssessment = group.getEvaluation_result().getManual_assessment_score();

        assertAll(
                ()->assertEquals(HttpStatus.OK, response.getStatusCode()),
                ()->assertTrue(group.isDone_manual_evaluation()),
                ()->assertEquals(50, manualAssessment),
                ()->assertEquals(50, group.getTotal_score())
        );
    }

    @Test
    @WithMockUser(username = "What am I doing here", authorities = {"STUDENT"})
    public void shouldNotSetManualScoreAsStudent(){
        ManualEvaluationRequest request = new ManualEvaluationRequest();
        request.setTournament_title("Tournament title");
        request.setBattle_title("Battle title");
        request.setGroup_id("ABCDEF0123456789ABBEDD01".toLowerCase());
        request.setPoints(50);

        var response = scoreService.setManualScores(request);

        assertAll(
                ()->assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()),
                ()->assertEquals("Cannot add manual evaluation as student", response.getBody().getError_msg())
        );
    }

    @Test
    public void shouldUpdateScoreAfterAutomaticEvaluation(){
        var groupId =  "ABCDEF0123456789ABBEDD01".toLowerCase();

        var tests_results = new HashMap<String, TestStatus>();
        tests_results.put("test_pass", TestStatus.PASSED);
        tests_results.put("test_fail", TestStatus.FAILED);

        var static_results = new HashMap<EvalParameter, Integer>();
        static_results.put(EvalParameter.QUALITY, 100);
        static_results.put(EvalParameter.SECURITY, 100);
        static_results.put(EvalParameter.RELIABILITY, 100);

        var evalResults = new EvaluationResult();
        evalResults.setTests_results(tests_results);
        evalResults.setStatic_analysis_results(static_results);
        evalResults.setTimeliness_score(50);

        scoreService.updateGroupAfterAutomaticEvaluation(groupId, evalResults);

        var tournament = mongoTemplate.findById("FFFFFF0123451989BBBBBB99", Tournament.class);
        assertNotNull(tournament);

        var group = tournament.getBattles().get(0).getGroups().get(0);

        int expected_score = (int)(0.3* 100 + 0.5 * 50 + 0.2 * 50 );

        assertEquals(expected_score, group.getTotal_score());
    }

    @Test
    public void shouldSetScoreAfterAutomaticEvaluation(){
        var groupId =  "ABCDEF0123456789ABBEDD01".toLowerCase();

        var tests_results = new HashMap<String, TestStatus>();
        tests_results.put("test_pass", TestStatus.PASSED);
        tests_results.put("test_fail", TestStatus.PASSED);

        var static_results = new HashMap<EvalParameter, Integer>();
        static_results.put(EvalParameter.QUALITY, 75);
        static_results.put(EvalParameter.SECURITY, 50);
        static_results.put(EvalParameter.RELIABILITY, 100);

        float expected_static_score = (75.0f + 50 + 100) /3;

        var evalResults = new EvaluationResult();
        evalResults.setTests_results(tests_results);
        evalResults.setStatic_analysis_results(static_results);
        evalResults.setTimeliness_score(50);

        var expected_automatic_score = (int) (0.3 * expected_static_score + 0.5 * 100 + 0.2 * 50);
        scoreService.updateGroupAfterAutomaticEvaluation(groupId, evalResults);

        var tournament = mongoTemplate.findById("FFFFFF0123451989BBBBBB99", Tournament.class);
        assertNotNull(tournament);

        var group = tournament.getBattles().get(0).getGroups().get(0);

        assertEquals(expected_automatic_score, group.getTotal_score());
    }

    @Test
    @WithMockUser(username = "I'm the one who knocks", authorities = {"EDUCATOR"})
    public void shouldUpdatePlayerRanksInTheTournamentWhenBattleEnds(){
        var groupId =  "ABCDEF0123456789ABBEDD01".toLowerCase();

        var tests_results = new HashMap<String, TestStatus>();
        tests_results.put("test_pass", TestStatus.FAILED);
        tests_results.put("test_fail", TestStatus.FAILED);

        var static_results = new HashMap<EvalParameter, Integer>();
        static_results.put(EvalParameter.QUALITY, 100);
        static_results.put(EvalParameter.SECURITY, 100);
        static_results.put(EvalParameter.RELIABILITY, 100);

        var evalResults = new EvaluationResult();
        evalResults.setTests_results(tests_results);
        evalResults.setStatic_analysis_results(static_results);
        evalResults.setTimeliness_score(50);

        var expected_automatic_score = (int) (0.3 * 100 + 0.5 * 0 + 0.2 * 50);
        scoreService.updateGroupAfterAutomaticEvaluation(groupId, evalResults);

        ManualEvaluationRequest request = new ManualEvaluationRequest();
        request.setTournament_title("Tournament title");
        request.setBattle_title("Battle title");
        request.setGroup_id("ABCDEF0123456789ABBEDD01".toLowerCase());
        request.setPoints(30);

        scoreService.setManualScores(request);

        // *battle ends*
        scoreService.updatePlayersRanks(groupId);

        var tournament = mongoTemplate.findById("FFFFFF0123451989BBBBBB99", Tournament.class);
        assertNotNull(tournament);

        var leaderboard = tournament.getSubscribed_users();
        var subscriber = leaderboard.get(0);

        var expected_final_score = (int)(0.7 * 30 + 0.3 * expected_automatic_score);
        assertEquals(expected_final_score, subscriber.getScore());
    }

    @Test
    @WithMockUser(username = "I'm the one who knocks", authorities = {"EDUCATOR"})
    public void shouldUpdatePlayerRanksEvenWithoutAutomaticAssessment(){
        var groupId =  "ABCDEF0123456789ABBEDD01".toLowerCase();

        ManualEvaluationRequest request = new ManualEvaluationRequest();
        request.setTournament_title("Tournament title");
        request.setBattle_title("Battle title");
        request.setGroup_id("ABCDEF0123456789ABBEDD01".toLowerCase());
        request.setPoints(30);

        scoreService.setManualScores(request);

        // *battle ends*
        scoreService.updatePlayersRanks(groupId);

        var tournament = mongoTemplate.findById("FFFFFF0123451989BBBBBB99", Tournament.class);
        assertNotNull(tournament);

        var leaderboard = tournament.getSubscribed_users();
        var subscriber = leaderboard.get(0);

        assertEquals(30, subscriber.getScore());
    }

}
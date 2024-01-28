package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
class TournamentServiceTest {
    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private TournamentService tournamentService;

    @Test
    @WithMockUser(username = "TestEducator", authorities = {"EDUCATOR"})
    public void shouldCreateTournamentCorrectly(){

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder().username("TestEducator").build());

        when(tournamentRepository.existsByTitle(anyString()))
                .thenReturn(false);

        TournamentCreationRequest request = new TournamentCreationRequest(
                "Test Tournament",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                List.of()
        );


        var response = tournamentService.createTournament(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @WithMockUser(username = "TestStudent", authorities = {"STUDENT"})
    public void shouldNotCreateTournamentAsStudent(){

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder().username("TestStudent").build());

        when(tournamentRepository.existsByTitle(anyString()))
                .thenReturn(false);

        TournamentCreationRequest request = new TournamentCreationRequest(
                "Test Tournament",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                List.of()
        );

        var response = tournamentService.createTournament(request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    @WithMockUser(username = "ginopippo", authorities = {"EDUCATOR"})
    public void shouldCreateTournamentCorrectly(){

        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(User.builder().username("ginopippo").build());

        when(tournamentRepository.existsByTitle(anyString()))
                .thenReturn(false);

        doNothing().when(notificationService).sendTournamentCreationNotifications(any());

        TournamentCreationRequest request = new TournamentCreationRequest(
                "Test Tournament",
                new Date(System.currentTimeMillis()+1000*60*60*24),
                List.of()
        );


        var response = tournamentService.createTournament(request);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    }
}
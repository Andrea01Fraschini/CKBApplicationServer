package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service that gathers information to construct the dashboard view for the user
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TournamentRepository tournamentRepository;
    public DashboardResponse getDashboard() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        String username = auth.getName();
        //Prendi le notifiche

        List<CardInfo> cards = new ArrayList<>();
        switch (accountType) {
            case STUDENT -> {
                //Collection<Tournament> tournaments = tournamentRepository.findTournamentsByStudent(username);
                System.out.println("STUDENT DASHBOARD");
            }
            case EDUCATOR -> {
                Collection<Tournament> tournaments = tournamentRepository.findTournamentsByEducator(username);

                for(var t : tournaments){
                    cards.add(new CardInfoEducator(
                            t.getTitle(),
                            t.getSubscribed_users().size(),
                            t.getBattles().size(),
                            t.getSubscription_deadline(),
                            t.getEducators()
                                    .stream()
                                    .map(e -> new CardInfoEducator.Educator(e.getUsername()))
                                    .toList()
                    ));
                }
            }
        }

        return DashboardResponse.builder()
                .account_type(accountType.name())
                .notifications(null)
                .cards(cards)
                .build();
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.search;


import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentController;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentManager;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository ;

    public List<TournamentController.TournamentsListEntry> searchTournament(String tournamentTitle){
        Collection<Tournament> tournaments = tournamentRepository.findByTitleSearch(tournamentTitle);
        return buildTournamentsInfo(tournaments);
    }

    public List<BattleInfo> searchBattle(String tournamentTitle, String battleTitle){
        var tournament = tournamentRepository.findTournamentByTitle(tournamentTitle);

        var pattern = Pattern.compile("(?i).*%s.*".formatted(battleTitle));
        var results = tournament.getBattles()
                .stream()
                .filter(battle -> {
                    Matcher matcher = pattern.matcher(battle.getTitle());
                    return matcher.matches();
                }).toList();

        return buildBattlesInfo(results, tournamentTitle);
    }

    private List<BattleInfo> buildBattlesInfo(List<Battle> results, String tournamentTitle){

        List<BattleInfo> battlesInfo = new ArrayList<>();
        Date today = new Date();

        for(var b : results){
            battlesInfo.add(new BattleInfo(
                    tournamentTitle,
                    b.getTitle(),
                    !today.after(b.getEnrollment_deadline()),
                    b.getEnrollment_deadline(),
                    b.getGroups().size()
            ));
        }

        return battlesInfo;
    }

    private List<TournamentController.TournamentsListEntry> buildTournamentsInfo(Collection<Tournament> tournaments){
        List<TournamentController.TournamentsListEntry> tournamentsListEntries = new ArrayList<>();

        for(var t : tournaments){
            tournamentsListEntries.add(new TournamentController.TournamentsListEntry(
                    t.getTitle(),
                    t.is_open(),
                    t.getSubscription_deadline(),
                    t.getSubscribed_users().size(),
                    t.getEducators().stream().map(TournamentManager::getUsername).toList()
            ));
        }

        return tournamentsListEntries;
    }

    public List<String> searchUser(String username){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());

        Collection<User> users = userRepository.findByAccountTypeAndUsernameLike(accountType, username);
        return users.stream().map(User::getUsername).toList();
    }

}

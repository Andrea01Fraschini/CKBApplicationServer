package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.LeaderboardEntry;
import BersaniChiappiniFraschini.CKBApplicationServer.search.BattleInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TournamentGetResponse {
    private boolean is_open;
    private String creator;
    private List<String> managers;
    private List<BattleInfo> battles;
    @Builder.Default
    private List<LeaderboardEntry> leaderboard = List.of();
    private Date subscription_deadline;
    private boolean already_subscribed;
}

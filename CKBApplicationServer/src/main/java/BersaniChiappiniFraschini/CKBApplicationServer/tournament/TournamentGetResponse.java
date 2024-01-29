package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.dashboard.CardInfo;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationDetails;
import BersaniChiappiniFraschini.CKBApplicationServer.search.BattleInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TournamentGetResponse {
    private List<BattleInfo> battleInfo;
    private Map<String, Integer> rankStudents;


    public void setRank(Map<String, Integer> points){
        this.rankStudents = points;
    }
}

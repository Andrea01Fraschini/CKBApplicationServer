package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentGetResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@Data
public class BattleInfoResponse {
    private Battle battle;
    private final List<PointGroup> leaderBoard = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class PointGroup {
        private String username;
        private Integer points;
    }

    public void addPoint(String leader, int score){
        this.leaderBoard.add(new PointGroup(leader, score));
    }

    public int getPoint(String leader){

        for(PointGroup p : leaderBoard){
            if(p.getUsername().equals(leader)){
                return p.getPoints();
            }
        }

        return 0;
    }
}

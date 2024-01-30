package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Builder
@AllArgsConstructor
@Data
public class BattleInfoResponseGeneral {
    private Battle battle;
    private final Map<String, Integer> leaderboard = new HashMap<>();

    public void addScore(String leader, int score) {
        this.leaderboard.put(leader, score);
    }

    public int getScore(String leader) {
        return this.leaderboard.get(leader) != null ? this.leaderboard.get(leader) : 0;
    }
}

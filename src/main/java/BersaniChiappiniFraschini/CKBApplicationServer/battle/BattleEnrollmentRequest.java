package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleEnrollmentRequest {
    private String tournament_title;
    private String battle_title;
    private List<String> invited_members;
}

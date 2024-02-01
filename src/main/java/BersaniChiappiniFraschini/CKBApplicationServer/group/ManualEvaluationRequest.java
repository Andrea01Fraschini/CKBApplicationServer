package BersaniChiappiniFraschini.CKBApplicationServer.group;

import lombok.Data;

@Data
public class ManualEvaluationRequest {
    private String tournament_id;
    private String battle_id;
    private String group_id;
    private int points;
}

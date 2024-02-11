package BersaniChiappiniFraschini.CKBApplicationServer.group;

import lombok.Data;

@Data
public class ManualEvaluationRequest {
    private String tournament_title;
    private String battle_title;
    private String group_id;
    private int points;
}

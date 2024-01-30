package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleCreationRequest {
    private String tournament_title;
    private String battle_title;
    private int min_group_size;
    private int max_group_size;
    private String description;
    private Date enrollment_deadline;
    private Date submission_deadline;
    private boolean manual_evaluation;
    private List<EvalParameter> evaluation_parameters;
    // TODO: add files ?
}

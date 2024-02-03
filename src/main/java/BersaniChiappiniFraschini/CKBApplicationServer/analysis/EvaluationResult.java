package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.TestStatus;
import lombok.Data;

import java.util.Map;

@Data
public class EvaluationResult {
    private Map<String, TestStatus> tests_results;
    private Map<EvalParameter, Integer> static_analysis_results;
    private Integer timeliness_score;
    private Integer manual_assessment_score;
}

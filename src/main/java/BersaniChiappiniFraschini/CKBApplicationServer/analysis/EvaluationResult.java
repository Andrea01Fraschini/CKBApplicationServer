package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.TestStatus;
import lombok.Data;

import java.util.Map;

@Data
public class EvaluationResult {
    private Map<String, TestStatus> testsResults;
    private Map<EvalParameter, Integer> staticAnalysisResults;
    private Integer timelinessScore;
    // private Integer manualAssessmentScore; // ?
}

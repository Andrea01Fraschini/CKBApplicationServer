package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;

import java.util.List;
import java.util.Map;

public interface ECARunner {
    Map<EvalParameter, Integer> launchExternalCodeAnalysis(String projectDirectory, List<EvalParameter> evaluationParameters);
}

package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for a generic External Code Analysis Runner.
 */
public interface ECARunner {
    /**
     * Launches the code analysis on external sources.
     * @param projectDirectory directory of the project to analyze.
     * @param evaluationParameters parameters to evaluate.
     * @return a map that associates to each of the given parameters to evaluate a numeric score.
     * @throws IOException when the files to analyze can't be found.
     */
    Map<EvalParameter, Integer> launchExternalCodeAnalysis(String projectDirectory, List<EvalParameter> evaluationParameters) throws IOException;
}

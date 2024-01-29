package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.JavaQualityAnalyzer;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.QualityScoreProcessor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class JavaECARunner implements ECARunner {
    // This could be over-engineered to have a Map of StaticAnalyzers, but that's too much work
    private final static JavaQualityAnalyzer qualityAnalyzer = new JavaQualityAnalyzer(new QualityScoreProcessor());
    private final static String DEFAULT_JAVA_VERSION = "17";
    private String jarPath = null;
    private String javaVersion = DEFAULT_JAVA_VERSION;

    public JavaECARunner(String jarPath) {
        this.jarPath = jarPath;
    }

    public JavaECARunner(String jarPath, String javaVersion) {
        this.jarPath = jarPath;
        this.javaVersion = javaVersion != null ? javaVersion : DEFAULT_JAVA_VERSION;
    }

    @Override
    public Map<EvalParameter, Integer> launchExternalCodeAnalysis(String projectDirectory, List<EvalParameter> evaluationParameters) {
        Map<EvalParameter, Integer> results = new HashMap<>();
        for (var param : evaluationParameters) {
            Integer result = null;
            switch (param) {
                case Quality -> result = qualityAnalyzer.runAnalysis(projectDirectory, jarPath, javaVersion);
            }
            if (result != null) results.put(param, result);
        }

        return results;
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.JavaQualityAnalyzer;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.JavaReliabilityAnalyzer;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.JavaSecurityAnalyzer;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs.QualityScoreProcessor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class JavaECARunner implements ECARunner {
    // This could be changed to a Map of StaticAnalyzers
    private final static JavaQualityAnalyzer qualityAnalyzer = new JavaQualityAnalyzer(new QualityScoreProcessor());
    private final static JavaReliabilityAnalyzer reliabilityAnalyzer = new JavaReliabilityAnalyzer();
    private final static JavaSecurityAnalyzer securityAnalyzer = new JavaSecurityAnalyzer();

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
                case Reliability -> result = reliabilityAnalyzer.runAnalysis();
                case Security -> result = securityAnalyzer.runAnalysis();
            }
            if (result != null) results.put(param, result);
        }

        return results;
    }
}

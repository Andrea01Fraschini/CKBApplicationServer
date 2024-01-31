package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs;

import lombok.RequiredArgsConstructor;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.lang.LanguageRegistry;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

@RequiredArgsConstructor
public class JavaQualityAnalyzer {
    private static final URL rulesetPath = JavaQualityAnalyzer.class.getClassLoader().getResource("ECAConfig/quickstart.xml");
    private final QualityScoreProcessor scoreProcessor;

    public int runAnalysis(String pathToCheck, String compiledPath, String javaVersion) {
        PMDConfiguration config = new PMDConfiguration();
        config.setDefaultLanguageVersion(LanguageRegistry.findLanguageByTerseName("java").getVersion(javaVersion));
        config.addInputPath(Path.of(pathToCheck));
        if (compiledPath != null) config.prependAuxClasspath(compiledPath); // path to jar, helps process checks
        config.addRuleSet(Objects.requireNonNull(rulesetPath).getPath());

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            Report report = pmd.performAnalysisAndCollectReport();
            var violations = report.getViolations();
            return scoreProcessor.computeScore(violations.size());
        }
    }
}

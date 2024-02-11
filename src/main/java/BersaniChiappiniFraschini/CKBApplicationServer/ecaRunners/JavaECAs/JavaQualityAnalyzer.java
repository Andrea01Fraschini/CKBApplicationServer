package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs;

import lombok.RequiredArgsConstructor;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.lang.LanguageRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * ECA - Runs static analysis for Quality on external source code.
 */
@RequiredArgsConstructor
public class JavaQualityAnalyzer {
    private static final InputStream rulesetStream = JavaQualityAnalyzer.class.getClassLoader().getResourceAsStream("ECAConfig/quickstart.xml");
    private final QualityScoreProcessor scoreProcessor;

    /**
     * Runs the static analysis on an external source.
     * @param pathToCheck path to the project to analyze.
     * @param compiledPath path to the compiled project (optional).
     * @param javaVersion version of Java for the project (i.e. 17)
     * @return the score assigned to the analyzed code.
     * @throws IOException when the ruleset or files to analyze can't be found.
     */
    public int runAnalysis(String pathToCheck, String compiledPath, String javaVersion) throws IOException {
        // Create temp file to allow the jar to see the ruleset file
        File rulesetTempFile = File.createTempFile("pmd_ruleset", ".xml");
        Files.copy(rulesetStream, rulesetTempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        rulesetTempFile.deleteOnExit();

        PMDConfiguration config = new PMDConfiguration();
        config.setDefaultLanguageVersion(LanguageRegistry.findLanguageByTerseName("java").getVersion(javaVersion));
        config.addInputPath(Path.of(pathToCheck));
        if (compiledPath != null) config.prependAuxClasspath(compiledPath); // path to jar, helps process checks
        config.addRuleSet(rulesetTempFile.getPath());

        try (PmdAnalysis pmd = PmdAnalysis.create(config)) {
            Report report = pmd.performAnalysisAndCollectReport();
            var violations = report.getViolations();
            return scoreProcessor.computeScore(violations.size());
        }
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.EvalParameter;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.ECARunner;
import BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECARunner;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.JavaTestRunner;
import BersaniChiappiniFraschini.CKBApplicationServer.testRunners.TestRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CodeAnalysisService {
    private record LanguageAnalysisTools(ProjectBuilder projectBuilder, ECARunner ecaRunner, TestRunner testRunner){}
    private static final Map<String, LanguageAnalysisTools> languageAnalysisTools = new HashMap<>();

    public CodeAnalysisService() {
        languageAnalysisTools.put("java 17", new LanguageAnalysisTools(new JavaProjectBuilder(), new JavaECARunner(), new JavaTestRunner()));
    }

    public static EvaluationResult launchAutomatedAssessment(String projectDirectory, String testFileName, List<EvalParameter> evaluationParameters, String language) throws Exception {
        var analysisTools = languageAnalysisTools.get(projectDirectory);
        // Build/compile project
        var compiledProjectPath = analysisTools.projectBuilder.buildProject(projectDirectory);
        // Run tests
        var testResults = analysisTools.testRunner.launchUnitTests(compiledProjectPath, testFileName);
        // Run Static Analysis
        var staticAnalysisResults = analysisTools.ecaRunner.launchExternalCodeAnalysis(projectDirectory, evaluationParameters);

        EvaluationResult result = new EvaluationResult();
        result.setTestsResults(testResults);
        result.setStaticAnalysisResults(staticAnalysisResults);

        return result;
    }
}

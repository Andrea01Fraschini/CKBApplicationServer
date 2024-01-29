package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import java.io.File;
import java.util.Map;

public interface TestRunner {
    Map<String, TestStatus> launchUnitTests(String projectDirectory, String testsFileName, String buildScriptFileName) throws Exception;
}

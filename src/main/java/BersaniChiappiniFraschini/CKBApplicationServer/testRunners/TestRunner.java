package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import java.io.File;
import java.util.Map;

public interface TestRunner {
    Map<String, TestStatus> launchUnitTests(String compiledProjectDirectory, String testsFileName) throws Exception;
}

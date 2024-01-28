package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import java.io.File;
import java.util.Map;

public interface TestRunner {
    public Map<String, TestStatus> launchUnitTests(String projectDirectory, File scripts);
}

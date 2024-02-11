package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import java.util.Map;

/**
 * Generic Test Runner. Runs tests on external source code.
 */
public interface TestRunner {
    /**
     * Launches unit tests on external sources.
     * @param compiledProjectDirectory path to the directory of the built project.
     * @param testsFileName name of the file containing the tests to run.
     * @return the results of the tests in the format (Name of testcase, Status), where status is either Failed or Passed.
     * @throws Exception when the project directory of the tests file are not found.
     */
    Map<String, TestStatus> launchUnitTests(String compiledProjectDirectory, String testsFileName) throws Exception;
}

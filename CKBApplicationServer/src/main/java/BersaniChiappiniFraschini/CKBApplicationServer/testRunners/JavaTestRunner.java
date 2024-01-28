package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class JavaTestRunner implements TestRunner {

    public Map<String, TestStatus> launchUnitTests(String projectDirectory, File scripts) {
        return runTests(TestTestClass.class);
    }

    private Map<String, TestStatus> runTests(Class<?> testClass) {
        Map<String, TestStatus> results = new HashMap<>();

        LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build();

        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.execute(discoveryRequest);

        TestExecutionSummary summary = summaryListener.getSummary();

        Method[] methods = testClass.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                DisplayName displayNameAnnotation = method.getAnnotation(DisplayName.class);
                // Initialize all as passed
                if (displayNameAnnotation == null) {
                    results.put(method.getName(), TestStatus.Passed);
                } else {
                    results.put(displayNameAnnotation.value(), TestStatus.Passed);
                }
            }
        }

        for (var failure : summary.getFailures()) {
            results.put(failure.getTestIdentifier().getDisplayName(), TestStatus.Failed);
        }

        return results;
    }
}

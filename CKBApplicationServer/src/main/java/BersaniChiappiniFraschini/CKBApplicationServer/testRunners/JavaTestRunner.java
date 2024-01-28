package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JavaTestRunner { // implements TestRunner

    // TODO: Remove jar path and build internally
    public Map<String, TestStatus> launchUnitTests(String jarFilePath, String fileName, File scripts) throws Exception {
        Class<?> testClass = loadTestClassFromJar(jarFilePath, fileName);
        return runTests(testClass);
    }

    // TODO: use scripts to compile
    private static void compileJavaFile(String filePath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, filePath);

        if (result != 0) {
            throw new RuntimeException("Compilation failed");
        }
    }

    private static Class<?> loadTestClassFromJar(String jarFilePath, String className) throws Exception {
        File jarFile = new File(jarFilePath);

        // Convert the JAR file path to URL with the "file" protocol
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});

        var canonicalName = findCanonicalNameInJar(jarFilePath, className);
        // Load the class dynamically
        return Class.forName(canonicalName, false, classLoader);
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
        // This is not particularly good but seems to work.
        for (var method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                DisplayName displayNameAnnotation = method.getAnnotation(DisplayName.class);
                // Initialize all as passed
                if (displayNameAnnotation == null) {
                    results.put(method.getName() + "()", TestStatus.Passed);
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

    // Finds the name of the package where the file is located in the jar file.
    public static String findCanonicalNameInJar(String jarFilePath, String targetClassName) throws IOException {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                var entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    var className = entryName.substring(entry.getName().lastIndexOf("/") + 1, entryName.lastIndexOf("."));
                    if (className.equals(targetClassName)) {
                        // Transform path to package name (Canonical name)
                        ClassReader classReader = new ClassReader(jarFile.getInputStream(entry));
                        ClassNameVisitor classNameVisitor = new ClassNameVisitor();
                        classReader.accept(classNameVisitor, 0);
                        return classNameVisitor.getClassName();
                    }
                }
            }
        }

        return null; // Class not found in the JAR
    }

    @Getter
    static class ClassNameVisitor extends ClassVisitor {
        private String className;

        public ClassNameVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name.replace('/', '.');
        }
    }
}

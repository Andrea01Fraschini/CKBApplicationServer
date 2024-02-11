package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JavaProjectBuilder implements ProjectBuilder {
    @Override
    public String buildProject(String projectDirectory) throws Exception {
        return buildProject(projectDirectory, true);
    }

    @Override
    public String buildProject(String projectDirectory, boolean debug) throws Exception {
        // Build the command to run the build script
        var directory = new File(projectDirectory);
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "build.sh");
        processBuilder.directory(directory); // look for build script in the project directory
        // Redirect the process output to the console
        processBuilder.redirectErrorStream(true);

        // Start the process
        Process process = processBuilder.start();

        if (debug) {
            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) throw new Exception("Build failure");

        return getJarPath(projectDirectory);
    }

    /**
     * Finds the path to the compiled jar.
     * @param projectPath path to the built project.
     * @return the path to the jar.
     * @throws Exception when the jar is not found.
     */
    private String getJarPath(String projectPath) throws Exception {
        String[] command = {"bash", "-c", "find %s -name '*.jar'".formatted(projectPath)};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // Redirect error stream to output stream
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Get the input stream from the process
        InputStream inputStream = process.getInputStream();
        // Create a BufferedReader to read the output
        var reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read the output line by line
        String output = reader.readLine();

        var exitCode = process.waitFor();
        if (exitCode != 0) throw new Exception("No file found");
        System.out.println("OUTPUT OF GETJARPATH: "+output);
        return output;
    }
}

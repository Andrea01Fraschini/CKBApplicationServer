package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

/**
 * Models a generic ProjectBuilder responsible for running the build script provided by educators.
 */
public interface ProjectBuilder {
    /**
     * Builds a project running the build.sh script (provided by educator) in the project directory.
     * @param projectDirectory directory of the project to build.
     * @return path to the built project.
     * @throws Exception when the build process fails.
     */
    String buildProject(String projectDirectory) throws Exception;

    /**
     * Builds a project running the build.sh script (provided by educator) in the project directory.
     * @param projectDirectory directory of the project to build.
     * @param debug if set to true enables logging during the build process.
     * @return path to the built project.
     * @throws Exception when the build process fails.
     */
    String buildProject(String projectDirectory, boolean debug) throws Exception;
}

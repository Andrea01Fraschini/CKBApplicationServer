package BersaniChiappiniFraschini.CKBApplicationServer.analysis;

public interface ProjectBuilder {
    String buildProject(String projectDirectory) throws Exception;
    String buildProject(String projectDirectory, boolean debug) throws Exception;
}

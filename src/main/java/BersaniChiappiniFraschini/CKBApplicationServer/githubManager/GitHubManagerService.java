package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.uploadFile.FilesStorageService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class GitHubManagerService {
    private final Environment environment;

    // Create Repository for the battle
    public String createRepository(String tournamentTitle, String battleTitle, String description) throws Exception {

        String githubToken = environment.getProperty("github.token");
        String owner = environment.getProperty("github.repo.owner");

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
            // Creation of new repository
            GHCreateRepositoryBuilder createRepositoryBuilder = github
                    .createRepository(tournamentTitle+"-"+battleTitle)
                    .autoInit(true)
                    .owner(owner)
                    .allowForking(true)
                    .gitignoreTemplate("Java")
                    .private_(false)
                    .description(description);

            GHRepository newRepository = createRepositoryBuilder.create();

            return newRepository.getHtmlUrl().toString();
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    // Upload the code of the battle
    public void setCodeRepository(String repo, String pathFile) throws Exception {
        String githubToken = environment.getProperty("github.token");
        String owner = environment.getProperty("github.repo.owner");

        String[] splitArray = repo.split("/");
        String name = splitArray[splitArray.length - 1];

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
            GHRepository repository = github.getRepository(owner + "/" + name);
            GHRef masterRef = repository.getRef("heads/main");
            String baseTreeSha = masterRef.getObject().getSha();


            GHTreeBuilder treeBuilder = repository.createTree();
            treeBuilder.baseTree(baseTreeSha);
            // build the tree with the files
            uploadDirectoryContents(new File(pathFile), "project", treeBuilder);

            // Create a new tree
            GHTree tree = treeBuilder.create();

            // Create a new commit
            GHCommit commit = repository.createCommit()
                    .message("Added project")
                    .tree(tree.getSha())
                    .parent(baseTreeSha)
                    .committer(owner, "code.kata.battle.bcf@example.com",new Date())
                    .create();

            GHRef localBranch = repository.getRef("heads/main");
            localBranch.updateTo(commit.getSHA1());

            protectRepo(repo);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // After all protect the repository only fork the group can do
    public void protectRepo(String repo){
        String[] splitArray = repo.split("/");
        String name = splitArray[splitArray.length - 1];

        String githubToken = environment.getProperty("github.token");
        String owner = environment.getProperty("github.repo.owner");

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();


            GHRepository repoGit = github.getRepository(owner+"/"+name);

            GHBranchProtectionBuilder protectionBuilder = repoGit.getBranch("main").enableProtection();
            protectionBuilder.includeAdmins(false);
            protectionBuilder.requireReviews();
            protectionBuilder.requireCodeOwnReviews(true);
            protectionBuilder.dismissStaleReviews(true);


            protectionBuilder.enable();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // DOWNLOAD FROM RESPOSITORY OF THE GROUP
    public String downloadRepo(String repo, String path){
        if(!repo.endsWith(".git")){
            repo = repo + ".git";
        }

        try {
            File localRepoDir = new File(path);

            // Clone the repository
            Git.cloneRepository()
                    .setURI(repo)
                    .setDirectory(localRepoDir)
                    .call()
                    .close();

            return localRepoDir.getAbsolutePath();
        } catch (GitAPIException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadDirectoryContents(File directory, String relativePath, GHTreeBuilder treeBuilder) throws Exception{
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                byte[] fileRead = Files.readAllBytes(file.toPath());
                treeBuilder = treeBuilder.add(relativePath + "/" + file.getName(), fileRead, false);
            } else if (file.isDirectory()) {
                uploadDirectoryContents(file, relativePath + "/" + file.getName(), treeBuilder);
            }
        }
    }

    @Async
    public CompletableFuture<String> saveFileAndCreateRepository(MultipartFile file, String battleTitle, String repo) {

        FilesStorageService filesStorageService = new FilesStorageService();
        String clearBattleName = battleTitle.replace(' ', '_');
        filesStorageService.init(clearBattleName);


        try {
            filesStorageService.save(file);
            String name = file.getOriginalFilename();

            if(name == null || name.equals("")){
                filesStorageService.deleteAll();
                return CompletableFuture.failedFuture(new Throwable("No name of the file"));
            }

            Resource resource = filesStorageService.load(name);

            File file1 = resource.getFile();
            InputStream i = file1.toURI().toURL().openStream();
            filesStorageService.unzip(i, clearBattleName);
            String path = filesStorageService.pathToGitHub();
            setCodeRepository(repo, path);
            protectRepo(repo);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new Throwable(e.getMessage()));
        } finally {
            filesStorageService.deleteAll();
        }

        return CompletableFuture.completedFuture(repo);
    }

}

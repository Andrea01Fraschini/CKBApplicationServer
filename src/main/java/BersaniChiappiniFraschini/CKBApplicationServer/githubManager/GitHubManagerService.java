package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GitHubManagerService {
    private final Environment environment;

    // we can use it to upload the file in a thread
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // Create Repository for the battle
    public String createRepository(String tournamentTitle, String battleTitle, String description){

        String githubToken = environment.getProperty("github.token");
        String owner = environment.getProperty("github.repo.owner");

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
            // Creazione della nuova repository
            GHCreateRepositoryBuilder createRepositoryBuilder = github
                    .createRepository(tournamentTitle+"-"+battleTitle)
                    .autoInit(true)
                    .owner(owner)
                    .allowForking(true)
                    .gitignoreTemplate("Java")
                    .private_(false)
                    .description(description);

            // Esecuzione effettiva della creazione
            GHRepository newRepository = createRepositoryBuilder.create();
            System.out.println("Nuova repository creata: " + newRepository.getHtmlUrl());

            return newRepository.getHtmlUrl().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Upload the code of the battle
    public boolean setCodeRepository(String repo, String pathFile, String battleTitle){
        String githubToken = environment.getProperty("github.token");
        String owner = environment.getProperty("github.repo.owner");

        String[] splittedArray = repo.split("/");
        String name = splittedArray[splittedArray.length - 1];

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(githubToken).build();
            GHRepository repository = github.getRepository(owner + "/" + name);
            GHRef masterRef = repository.getRef("heads/main");  // Sostituisci con il nome della tua branch
            String baseTreeSha = masterRef.getObject().getSha();


            GHTreeBuilder treeBuilder = repository.createTree();
            treeBuilder.baseTree(baseTreeSha);
            // build the tree with the files
            uploadDirectoryContents(new File(pathFile), battleTitle, treeBuilder);

            // Crea un nuovo albero
            GHTree tree = treeBuilder.create();

            // Crea un nuovo commit
            GHCommit commit = repository.createCommit()
                    .message("Added project")
                    .tree(tree.getSha())
                    .parent(baseTreeSha)
                    .committer(owner, "code.kata.battle.bcf@example.com",new Date())
                    .create();

            GHRef localBranch = repository.getRef("heads/main");
            localBranch.updateTo(commit.getSHA1());

            protectRepo(repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    // After all protect the repository only fork the group can do
    public void protectRepo(String repo){
        String[] splittedArray = repo.split("/");
        String name = splittedArray[splittedArray.length - 1];

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
            if(file.getName().equals(".idea") || file.getName().equals("target")){
                continue;
            }else if (file.isFile()) {

                System.out.println(file.toURI());

                byte[] fileRead = Files.readAllBytes(file.toPath());

                treeBuilder = treeBuilder.add(relativePath + "/" + file.getName(), fileRead, false);

            } else if (file.isDirectory()) {
                uploadDirectoryContents(file, relativePath + "/" + file.getName(), treeBuilder);
            }
        }
    }

    @Builder
    @Data
    @AllArgsConstructor
    static class ResponseRequest{
        int code;
        Map body;
    }

}

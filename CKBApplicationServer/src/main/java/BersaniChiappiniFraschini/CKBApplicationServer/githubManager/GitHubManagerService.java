package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.*;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.io.IOUtils;
import org.json.HTTP;
import org.json.JSONObject;
import org.kohsuke.github.*;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class GitHubManagerService {
    private final Environment environment;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private ArrayList<Pair<String, String>> runner = new ArrayList<>();

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    // TODO: After all protect the repository only fork the group can do

    // TODO: DOWNLOAD FROM RESPOSITORY OF THE GROUP


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

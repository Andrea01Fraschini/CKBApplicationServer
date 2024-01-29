package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.*;
import org.antlr.v4.runtime.misc.Pair;
import org.json.HTTP;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class GitHubManagerService {
    private final Environment environment;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private ArrayList<Pair<String, String>> runner = new ArrayList<>();

    //Create Repository for the battle
    public String createRepository(String tournamentTitle, String battleTitle, String description){
        String apiUrl = "/user/repos";
        HttpMethod httpMethod = HttpMethod.POST;
        String requestBody =  "{\n" +
                "    \"name\": \""+tournamentTitle+"-"+battleTitle+"\",\n" +
                "    \"description\": \""+description+"\",\n" +
                "    \"homepage\": \"battle_ckb\",\n" +
                "    \"private\": false,\n" +
                "    \"visibility\": \"public\",\n" +
                "    \"auto_init\": true,\n" +
                "    \"allow_squash_merge\": false,\n" +
                "    \"allow_merge_commit\": false,\n" +
                "    \"allow_auto_merge\": false\n" +
                "}";

        ResponseRequest response = sendRequest(httpMethod, apiUrl, requestBody);

        if (response.code == HttpStatus.CREATED.value()) {
            return (String) response.body.get("html_url");
        } else {
            return "ERROR";
        }
    }

    //Upload the code of the battle
    public boolean setCodeRepository(String repository, String pathFile){

        String[] splittedArray = repository.split("/");
        String name = splittedArray[splittedArray.length - 1];
        
        try {
            uploadDirectoryContents(new File(pathFile), "", name);

            Runnable request = () -> {

                for(Pair<String, String> r : runner){
                    uploadFileToGitHub(r.a, r.b, name);
                }
            };

            executor.submit(request);

            return true;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }

    }

    //After all protect the repository only fork the group can do


    private ResponseRequest sendRequest(HttpMethod httpMethod, String apiUrl, String requestBody) {
        String githubApiUrlBase = environment.getProperty("github.api.url");
        String githubToken = environment.getProperty("github.token");

        HttpResponse<Map> response = null;

        try {
            Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
                final ObjectMapper mapper = new ObjectMapper();
                @SneakyThrows
                public String writeValue(Object value) {
                    return mapper.writeValueAsString(value);
                }
                @SneakyThrows
                public <T> T readValue(String value, Class<T> valueType) {
                    return mapper.readValue(value, valueType);
                }
            });

            if(httpMethod == HttpMethod.POST) {
                response = Unirest.post(githubApiUrlBase + apiUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + githubToken)
                        .body(requestBody).asObject(Map.class);
            }else if(httpMethod == HttpMethod.PUT){
                response = Unirest.put(githubApiUrlBase + apiUrl)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + githubToken)
                        .body(requestBody).asObject(Map.class);
            }

        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

        return ResponseRequest.builder()
                .code(response.getStatus())
                .body(response.getBody())
                .build();
    }

    private void uploadDirectoryContents(File directory, String relativePath, String repo) throws Exception{
        for (File file : directory.listFiles()) {
            if (file.isFile()) {

                String base = getFile(file);

                if(base == null){
                    throw new Exception("Null bas");
                }

                runner.add(new Pair<>(base, relativePath + "/" + file.getName()));

            } else if (file.isDirectory()) {
                uploadDirectoryContents(file, relativePath + "/" + file.getName(), repo);
            }
        }
    }

    private String getFile(File directory){
        Path folderPath = directory.toPath();
        String codeBase64;
        byte[] file;


        try {
            file = Files.readAllBytes(folderPath);
            codeBase64 = Base64.getEncoder().encodeToString(file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            codeBase64 = null;
        }

        return codeBase64;
    }

    private void uploadFileToGitHub(String codeBase64, String realtive,String repo) {

        String repoOwner = environment.getProperty("github.repo.owner");
        String apiUrl = "/repos/"+repoOwner+"/"+repo+"/contents/project8"+realtive;
        HttpMethod httpMethod = HttpMethod.PUT;

        String requestBody =  "{" +
                "\n" +
                "    \"message\":\"Added the code for the battle "+realtive+"\",\n" +
                "    \"committer\": {\"name\":\""+repoOwner+"\",\"email\":\"code.kata.battle.git@github.com\"},\n" +
                "    \"content\": \""+codeBase64+"\"\n" +
                "}";

       sendRequest(httpMethod, apiUrl, requestBody);
    }

    @Builder
    @Data
    @AllArgsConstructor
    static class ResponseRequest{
        int code;
        Map body;
    }

}

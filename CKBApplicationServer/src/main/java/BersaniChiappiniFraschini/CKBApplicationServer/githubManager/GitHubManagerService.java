package BersaniChiappiniFraschini.CKBApplicationServer.githubManager;

import BersaniChiappiniFraschini.CKBApplicationServer.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.*;
import org.json.JSONObject;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubManagerService {
    private final Environment environment;


    //Create Repository for the battle
    public String createRepository(String tournamentTitle, String battleTitle){
        String apiUrl = "/user/repos";
        HttpMethod httpMethod = HttpMethod.POST;
        String requestBody =  "{\n" +
                "    \"name\": \""+tournamentTitle+"-"+battleTitle+"\",\n" +
                "    \"description\": \"description\",\n" +
                "    \"homepage\": \"battle_ckb\",\n" +
                "    \"private\": false,\n" +
                "    \"visibility\": \"public\",\n" +
                "    \"auto_init\": true,\n" +
                "    \"allow_squash_merge\": false,\n" +
                "    \"allow_merge_commit\": false,\n" +
                "    \"allow_auto_merge\": false\n" +
                "}";


        ResponseRequest response = sendRequest(httpMethod, apiUrl, requestBody);

        if(response.code == HttpStatus.CREATED.value()){
            return (String) response.body.get("html_url");
        } else {
            return "ERROR";
        }
    }

    //Upload the code of the battle
    public boolean setCodeRepository(String repository, String pathFile){
        String codeBase64 = getFile(pathFile);

        String[] splittedArray = repository.split("/");
        String name = splittedArray[splittedArray.length - 1];

        if(codeBase64 == null) return false;

        String repoOwner = environment.getProperty("github.repo.owner");
        String apiUrl = "/repos/"+repoOwner+"/"+name+"/contents/project";
        HttpMethod httpMethod = HttpMethod.POST;

        String requestBody =  "{" +
                "\n" +
                "    \"message\":\"Added the code for the battle\",\n" +
                "    \"committer\": {\"name\":\""+repoOwner+"\",\"email\":\"code.kata.battle.git@github.com\"},\n" +
                "    \"content\": \""+codeBase64+"\"\n" +
                "}";


        ResponseRequest response = sendRequest(httpMethod, apiUrl, requestBody);

        return response.code == HttpStatus.ACCEPTED.value();
    }

    //After all protect the repository only fork the group can do


    private ResponseRequest sendRequest(HttpMethod httpMethod, String apiUrl, String requestBody){
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


    private String getFile(String filePath){
        File folder = new File(filePath);
        Path folderPath = folder.toPath();
        byte[] file;
        try {
            file = Files.readAllBytes(folderPath);
            return Base64.getEncoder().encodeToString(file);
        } catch (IOException e) {
            return null;
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

package BersaniChiappiniFraschini.CKBApplicationServer;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.githubManager.GitHubManagerService;
import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final JwtService jwtService;
    private final test_groupRepository test_groupRepository;
    private final GitHubManagerService gitHubManagerService;

    @GetMapping
    public ResponseEntity<String> test(
            //@RequestBody TournamentCreationRequest request
            ){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return ResponseEntity.ok("Hello %s".formatted(name));
    }

    /*@Bean
    public CommandLineRunner runner(){
        return args -> {
            //var file = context.getResource("classpath:test_file.txt").getFile();
            try{
                try(var create = new FileOutputStream("./test_file.txt")){
                    create.write("======== TEST FILE ========".getBytes(StandardCharsets.UTF_8));
                }
                try(var file = new FileInputStream("./test_file.txt")){
                    System.out.println(new String(file.readAllBytes()));
                }
            }catch (Exception ignored){}

        };
    }*/

    @PostMapping("/push")
    public ResponseEntity<String> test2(
            @RequestHeader(name = "Authorization") String authorization
    ) throws IOException {
        var token = authorization.substring(7);
        var groupId = jwtService.extractUsername(token);
        var group = test_groupRepository.findById(groupId);
        //var group = mongoTemplate.findById(groupId, Group.class, "test_group");
        String repository = group.get().getRepository();

        System.out.println("A message from %s:".formatted(repository));

        gitHubManagerService.downloadRepo(repository, "./repos/");

        try{
            try(var file = new FileInputStream("./repos/message.txt")){
                System.out.println(new String(file.readAllBytes()));
            }
        }catch (Exception ignored){}

        deleteDirectory(new File("./repos"));

        return ResponseEntity.ok("received !");
    }


    private static void deleteDirectory(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteDirectory(child);
            }
        }
        file.delete();
    }
}

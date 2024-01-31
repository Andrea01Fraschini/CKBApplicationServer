package BersaniChiappiniFraschini.CKBApplicationServer.authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest request
    ){
        return authService.login(request);
    }

    @GetMapping("/test")
    public String test(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        String message = "Hello %s".formatted(name);

        return "Hello there!";
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
}

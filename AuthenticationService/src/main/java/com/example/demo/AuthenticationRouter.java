package com.example.demo;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
public class AuthenticationRouter {

    AuthenticationService authenticationService;

    @GetMapping(path = "/registerNewAccount")
    public MessageReturn registerNewAccount(){
        // TODO insert the reading of the body
        return authenticationService.insertNewAccount("prova4", "prova2@gmail.com", "eccoci");
    }

    @PostMapping(path = "/login")
    public String login(){
        return "DENTRO login";
    }

    @GetMapping("/test")
    public void getTest(){

    }
}

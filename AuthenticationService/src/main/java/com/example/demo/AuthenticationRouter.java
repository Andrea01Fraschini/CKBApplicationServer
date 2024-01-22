package com.example.demo;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
public class AuthenticationRouter {

    AuthenticationService authenticationService;

    @PostMapping(path = "/registerNewAccount")
    public String registerNewAccount(){
        return "DENTRO registerNewAccount";
    }

    @PostMapping(path = "/login")
    public String login(){
        return "DENTRO login";
    }

    @GetMapping("/test")
    public void getTest(){

    }
}

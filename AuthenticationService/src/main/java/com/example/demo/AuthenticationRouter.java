package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class AuthenticationRouter {

    AuthenticationBusiness authenticationBusiness;

    @PostMapping(path = "/registerNewAccount")
    public String registerNewAccount(){
        return "DENTRO registerNewAccount";
    }

    @PostMapping(path = "/login")
    public String login(){
        return "DENTRO login";
    }
}

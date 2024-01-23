package com.example.demo;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1")
@AllArgsConstructor
public class AuthenticationRouter {
    private AuthenticationService authenticationService;

    @PostMapping(path = "/registerNewAccount")
    public ResponseEntity<MessageReturn> registerNewAccount(@Valid @RequestBody RequestNewAccount request){
        try {
            // insert the reading of the body
            return new ResponseEntity<>(authenticationService.insertNewAccount(request.getUsername(), request.getEmail(), request.getPassword()), HttpStatus.CREATED);
            // return new ResponseEntity<>(request,HttpStatus.BAD_REQUEST);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(300, "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "/login")
    public String login(){
        return "DENTRO login";
    }

    @GetMapping("/test")
    public void getTest(){

    }
}

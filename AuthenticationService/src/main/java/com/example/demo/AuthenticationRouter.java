package com.example.demo;

import com.example.demo.RequestMessage.RequestAuth;
import com.example.demo.RequestMessage.RequestGenerateToken;
import com.example.demo.RequestMessage.RequestNewAccount;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(path = "/auth")
    public ResponseEntity<MessageReturn> auth(@Valid @RequestBody RequestAuth requestAuth){
        try{
            return new ResponseEntity<>(authenticationService.authentication(requestAuth.getKey(), requestAuth.getValue()), HttpStatus.ACCEPTED);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(300, "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path ="/generateAuthToken")
    public ResponseEntity<MessageReturn> generateAPIAuthToken(@Valid @RequestBody RequestGenerateToken requestGenerateToken){
        try{
            return new ResponseEntity<>(authenticationService.createAPIAuthToken(requestGenerateToken.getId()), HttpStatus.ACCEPTED);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(300, "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }
}

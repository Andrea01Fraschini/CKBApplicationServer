package BersaniChiappiniFraschini.AuthenticationService;

import BersaniChiappiniFraschini.AuthenticationService.requestMessage.RequestAuth;
import BersaniChiappiniFraschini.AuthenticationService.requestMessage.RequestToken;
import BersaniChiappiniFraschini.AuthenticationService.requestMessage.RequestNewAccount;
import BersaniChiappiniFraschini.AuthenticationService.returnMessage.MessageReturn;
import BersaniChiappiniFraschini.AuthenticationService.returnMessage.ReturnCode;
import BersaniChiappiniFraschini.AuthenticationService.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("api/v1")
public class AuthenticationRouter {
    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationRouter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/registerNewAccount")
    public ResponseEntity<MessageReturn> registerNewAccount(@Valid @RequestBody RequestNewAccount request){
        try {
            return new ResponseEntity<>(authenticationService.insertNewAccount(request.getUsername(), request.getEmail(), request.getPassword()), HttpStatus.CREATED);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(ReturnCode.NOT_FORMAT_REQUEST.getDefaultMessage(), "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path = "/auth")
    public ResponseEntity<MessageReturn> auth(@Valid @RequestBody RequestAuth requestAuth){
        try{
            return new ResponseEntity<>(authenticationService.authentication(requestAuth.getKey(), requestAuth.getValue()), HttpStatus.ACCEPTED);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(ReturnCode.NOT_FORMAT_REQUEST.getDefaultMessage(), "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(path ="/generateAuthToken")
    public ResponseEntity<MessageReturn> generateAPIAuthToken(@Valid @RequestBody RequestToken requestToken){
        try{
            return new ResponseEntity<>(authenticationService.createAPIAuthToken(requestToken.getId()), HttpStatus.ACCEPTED);
        }catch (NullPointerException e){
            return new ResponseEntity<>(new MessageReturn(ReturnCode.NOT_FORMAT_REQUEST.getDefaultMessage(), "not correct structure of the request"), HttpStatus.BAD_REQUEST);
        }
    }
}

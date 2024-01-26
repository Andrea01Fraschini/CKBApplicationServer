package BersaniChiappiniFraschini.AuthenticationService.security;

import BersaniChiappiniFraschini.AuthenticationService.returnMessage.MessageReturn;
import BersaniChiappiniFraschini.AuthenticationService.returnMessage.ReturnCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

// handler the logic
@ControllerAdvice
public class RequestExceptionHandler {

    //when an error like MethodArgumentNotValidException is raised so it will be handled by this method
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageReturn> handlerInvalidArgument(MethodArgumentNotValidException ex){
        Map<String, String> errorMap = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));

        MessageReturn messageReturn = MessageReturn.builder()
                .code(ReturnCode.NOT_FORMAT_REQUEST.getDefaultMessage())
                .message(errorMap.toString())
                .build();

        return new ResponseEntity<>(messageReturn, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageReturn> handlerInvalidArgument(HttpMessageNotReadableException ex){
        return new ResponseEntity<>(new MessageReturn(ReturnCode.NOT_FORMAT_REQUEST.getDefaultMessage(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}

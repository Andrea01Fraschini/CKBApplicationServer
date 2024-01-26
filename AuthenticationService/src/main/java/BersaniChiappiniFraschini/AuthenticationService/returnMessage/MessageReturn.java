package BersaniChiappiniFraschini.AuthenticationService.returnMessage;


import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MessageReturn implements Serializable {
    private final int code;
    private final String message;

    public MessageReturn(int code, String message){
        this.code = code;
        this.message = message;
    }
}

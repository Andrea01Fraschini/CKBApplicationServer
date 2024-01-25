package BersaniChiappiniFraschini.AuthenticationService.requestMessage;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RequestToken {
    @NotNull
    @NotBlank
    private String id;
}

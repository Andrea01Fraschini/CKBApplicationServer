package BersaniChiappiniFraschini.AuthenticationService.requestMessage;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RequestAuth {
    @NotNull
    @NotBlank
    private String key;
    @NotNull
    @NotBlank
    private String value;
}

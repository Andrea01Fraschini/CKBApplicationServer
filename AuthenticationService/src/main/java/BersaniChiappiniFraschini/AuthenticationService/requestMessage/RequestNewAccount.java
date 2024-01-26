package BersaniChiappiniFraschini.AuthenticationService.requestMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor(staticName = "build")
@NoArgsConstructor
public class RequestNewAccount {
    @NotBlank
    @NotNull(message = "Username shouldn't be null")
    private String username;
    @NotBlank
    @NotNull(message = "Email shouldn't be null")
    @Email(message = "Invalid structure of the e-mail")
    private String email;
    @NotBlank
    @NotNull(message = "Password shouldn't be null")
    private String password;
}

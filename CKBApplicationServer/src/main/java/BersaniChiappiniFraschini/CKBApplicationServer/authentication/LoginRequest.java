package BersaniChiappiniFraschini.CKBApplicationServer.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email_or_username;
    private String password;
}

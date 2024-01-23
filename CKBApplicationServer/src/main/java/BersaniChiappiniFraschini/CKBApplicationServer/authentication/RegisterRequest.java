package BersaniChiappiniFraschini.CKBApplicationServer.authentication;

import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private AccountType account_type;
}

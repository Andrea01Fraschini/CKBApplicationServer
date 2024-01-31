package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerInviteRequest {
    private String tournament_title;
    private String username;
}

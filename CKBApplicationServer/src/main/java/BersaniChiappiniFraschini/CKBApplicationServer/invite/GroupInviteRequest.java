package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInviteRequest {
    private String tournament_title;
    private String battle_title;
    private String username;
}

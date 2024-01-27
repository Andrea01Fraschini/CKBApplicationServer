package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteStatusUpdateRequest {
    private String invite_id;
    private String tournament_id;
    private boolean accepted;
}

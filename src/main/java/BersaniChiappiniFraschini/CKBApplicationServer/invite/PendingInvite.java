package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class PendingInvite {
    private String id;

    public PendingInvite(User user) {
        this.id = user.getId();
    }
}

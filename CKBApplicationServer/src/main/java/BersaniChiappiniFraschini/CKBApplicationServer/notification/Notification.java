package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import BersaniChiappiniFraschini.CKBApplicationServer.invite.Invite;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Notification {
    @Id
    private String id;
    @Indexed
    private String username;
    private String message;
    private Date creation_date;
    private boolean is_closed;
    private Invite invite;
}

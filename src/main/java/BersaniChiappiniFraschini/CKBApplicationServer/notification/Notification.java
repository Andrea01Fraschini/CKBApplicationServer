package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    private String id;
    private String message;
    private NotificationType type;
    private Date creation_date;
    private boolean is_closed;
    //private Invite invite; // Maybe it's not necessary
}

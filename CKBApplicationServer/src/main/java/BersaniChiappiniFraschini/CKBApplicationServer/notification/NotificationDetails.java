package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetails { // Used for responses
    private String id;
    private String message;
    private String type;
    private String context;
}

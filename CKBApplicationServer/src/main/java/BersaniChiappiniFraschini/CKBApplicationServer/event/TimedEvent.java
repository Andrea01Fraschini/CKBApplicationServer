package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimedEvent {
    private String scheduledTime;
    private String eventName;
}

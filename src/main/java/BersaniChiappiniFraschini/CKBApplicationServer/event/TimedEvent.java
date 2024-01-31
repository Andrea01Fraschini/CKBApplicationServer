package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class TimedEvent {
    private String eventName;
    private long scheduleDelay;

    public TimedEvent(String eventName, Date date) {
        this.eventName = eventName;
        this.scheduleDelay = Math.max(0, date.getTime() - System.currentTimeMillis());
    }
}

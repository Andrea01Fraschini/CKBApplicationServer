package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Event that triggers at a given time
 */
@Data
@NoArgsConstructor
public class TimedEvent {
    private String eventName;
    private long scheduleDelay;

    /**
     * @param eventName name of the event.
     * @param date date-time when to schedule the event.
     */
    public TimedEvent(String eventName, Date date) {
        this.eventName = eventName;
        this.scheduleDelay = Math.max(0, date.getTime() - System.currentTimeMillis());
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles events on the platform.
 */
@Service
@NoArgsConstructor
public class EventService {
    private static final ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(10);
    private static final Map<String, Runnable> registeredEvents = new HashMap<>();

    /**
     * Registers a timed event and schedules it to a given time.
     * @param event timed event to schedule.
     * @param handler handler called when the scheduled time comes.
     */
    public static void registerTimedEvent(TimedEvent event, Runnable handler) {
        taskScheduler.schedule(handler, event.getScheduleDelay(), TimeUnit.MILLISECONDS);
    }

    /**
     * Register a named event.
     * @param eventName name of the event.
     * @param handler handler called when the event needs to be handled.
     */
    public static void registerEvent(String eventName, Runnable handler) {
        registeredEvents.put(eventName, handler);
    }

    /**
     * Handles an event given its name.
     * @param eventName name of the event to handle.
     */
    public static void handleEvent(String eventName) {
        var event = registeredEvents.get(eventName);
        if (event != null) {
            event.run();
        }
    }
}

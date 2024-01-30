package BersaniChiappiniFraschini.CKBApplicationServer.event;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EventService {
    private static final ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(10);
    private static final Map<String, Runnable> registeredEvents = new HashMap<>();

    public EventService() {
        // TODO: add handlers here
        registeredEvents.put("GitHubPushAction", () -> {});
    }

    public static void registerTimedEvent(TimedEvent event, Runnable handler) {
        taskScheduler.schedule(handler, event.getScheduleDelay(), TimeUnit.MILLISECONDS);
    }

    public static void registerEvent(String eventName, Runnable handler) {
        registeredEvents.put(eventName, handler);
    }

    public static void handleEvent(String eventName) {
        var event = registeredEvents.get(eventName);
        if (event != null) {
            event.run();
        }
    }
}

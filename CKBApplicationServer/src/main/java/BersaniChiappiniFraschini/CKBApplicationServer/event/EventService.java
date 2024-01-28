package BersaniChiappiniFraschini.CKBApplicationServer.event;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EventService {
    private final ScheduledExecutorService taskScheduler;
//    private final Map<String, Runnable> registeredEvents = new HashMap<>();

    public void registerTimedEvent(TimedEvent event, Runnable handler) {
        taskScheduler.schedule(handler, event.getScheduleDelay(), TimeUnit.MILLISECONDS);
    }

//    public void registerEvent(String eventName, Runnable handler) {
//        registeredEvents.put(eventName, handler);
//    }
//
//    public void handleEvent(String eventName) {
//        var event = registeredEvents.get(eventName);
//        if (event != null) {
//            event.run();
//        }
//    }

    public ResponseEntity<PostResponse> handlePushEvent(String repositoryUrl) {
        return ResponseEntity.ok(null);
    }
}

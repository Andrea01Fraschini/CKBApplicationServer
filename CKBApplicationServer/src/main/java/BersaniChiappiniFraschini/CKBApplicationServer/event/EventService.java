package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class EventService {
    private final ThreadPoolTaskScheduler taskScheduler;
    private final List<ScheduledFuture<?>> scheduledEvents;

    public void registerTimedEvent(TimedEvent event, Runnable handler) {
        var scheduledFuture = taskScheduler.schedule(handler, new CronTrigger(event.getScheduledTime()));
        scheduledEvents.add(scheduledFuture);
    }

    public void handlePushEvent(String repositoryUrl) {

    }
}

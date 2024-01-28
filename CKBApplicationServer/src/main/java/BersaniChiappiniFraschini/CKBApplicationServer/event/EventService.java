package BersaniChiappiniFraschini.CKBApplicationServer.event;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EventService {
    private final ScheduledExecutorService taskScheduler;

    public void registerTimedEvent(TimedEvent event, Runnable handler) {
        taskScheduler.schedule(handler, event.getScheduleDelay(), TimeUnit.MILLISECONDS);
    }

    public void handlePushEvent(String repositoryUrl) {

    }
}

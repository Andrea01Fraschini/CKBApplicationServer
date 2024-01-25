package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Collection<Notification> findNotificationsByUsername(String user);
}

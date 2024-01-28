package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Service that allows to send and store notifications
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Sends a notification to all the students about the creation of a new tournament
     * @param tournament new tournament
     */
    public void sendTournamentCreationNotifications(Tournament tournament){
        String message = "A tournament titled '%s' has been created by '%s'. Go ahead and join it!"
                .formatted(tournament.getTitle(), tournament.getEducators().get(0).getUsername());


        for(var email : userRepository.getAllStudentsEmails()){
            sendNotification(email.getEmail(), message);
        }
    }

    /**
     * Sends a notification to all subscribed students in the tournament of a new battle
     * @param battle new battle
     * @param tournament tournament where the battle can ba found
     */
    public void sendBattleCreationNotification(Battle battle, Tournament tournament) {
        String message = "A battle titled '%s' has been created in tournament '%s'."
                .formatted(battle.getTitle(), tournament.getTitle());

        for (var user : tournament.getSubscribed_users()) {
            sendNotification(user.getEmail(), message);
        }
    }

    public void sendInviteNotification(User sender, User receiver) {
        String message = "You received an invite from %s"
                .formatted(sender.getUsername());

        sendNotification(receiver.getEmail(), message);
    }

    public void sendInviteStatusUpdate(User sender, boolean accepted) {
        String message = "%s has %s your invite"
                .formatted(sender.getUsername(), accepted ? "accepted" : "rejected");

        sendNotification(sender.getEmail(), message);
    }

    public void sendRepositoryInvites(Group group, Battle battle, String APIToken) {
        var message = "The battle '%s' has started! You can find the repository at the following link: %s. Remember to include your group access token: %s"
                .formatted(battle.getTitle(), battle.getRepository(), APIToken);

        for (var member : group.getMembers()) {
            sendNotification(member.getEmail(), message);
        }
    }

    /**
     * Sends a notification to a user
     * @param user_email User email used for identification and for sending an email
     * @param message body of the notification
     */
    public void sendNotification(String user_email, String message){
        var notification = Notification.builder()
                .id(ObjectId.get().toString())
                .message(message)
                .is_closed(false)
                .creation_date(new Date(System.currentTimeMillis()))
                .build();

        userService.addNotification(user_email, notification);

        sendEmail(user_email, "New notification from CodeKataBattle",
                "You received a notification from code kata battle:\n\n" +
                        "'%s'".formatted(message));
    }


    /**
     * Sends an email
     * @param receiver_address receiver's email address
     * @param subject subject of the email
     * @param message text body of the email
     */
    private void sendEmail(String receiver_address, String subject, String message){
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("noreply@codekattabattle.com");
        email.setTo(receiver_address);
        email.setSubject(subject);
        email.setText("%s\n\n-CodeKataBattle notification service".formatted(message));
        try{
            emailSender.send(email);
        }catch (Exception ignored){

        }
    }
}

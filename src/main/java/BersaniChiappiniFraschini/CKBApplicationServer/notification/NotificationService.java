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
            sendNotification(email.getEmail(), message, NotificationType.NEW_TOURNAMENT);
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
            sendNotification(user.getEmail(), message, NotificationType.NEW_BATTLE);
        }
    }

    public void sendInviteNotification(User sender, User receiver) {
        String message = "You received an invite from %s"
                .formatted(sender.getUsername());

        sendNotification(receiver.getEmail(), message, NotificationType.NEW_INVITE);
    }

    public void sendInviteStatusUpdate(User sender, User receiver, boolean accepted) {
        String message = "%s has %s your invite"
                .formatted(receiver.getUsername(), accepted ? "accepted" : "rejected");

        sendNotification(sender.getEmail(), message, NotificationType.INVITE_STATUS_UPDATE);
    }

    public void sendRepositoryInvites(Group group, Battle battle, String APIToken) {
        var message = "The battle '%s' has started! You can find the repository at the following link: %s. Remember to include your group access token: %s"
                .formatted(battle.getTitle(), battle.getRepository(), APIToken);

        for (var member : group.getMembers()) {
            sendNotification(member.getEmail(), message, NotificationType.NEW_REPOSITORY_INVITE);
        }
    }

    public void sendGroupRemovedFromBattle(Group group, Battle battle) {
        var message = "\n" +
                "Your group has been removed from the battle '%s' because the number of participants does not meet the battle requirements"
                .formatted(battle.getTitle());

        for (var member : group.getMembers()) {
            sendNotification(member.getEmail(), message, NotificationType.GROUP_REMOVED_FROM_BATTLE);
        }
    }

    public void sendNewBattleRankAvailable(Group group, String tournamentTitle, String battleTitle) {
        var message = "\n" +
                "Final ranking of the battle '%s' in the tournament '%s' now available, hurry and see it!!!"
                        .formatted(battleTitle, tournamentTitle);

        for (var member : group.getMembers()) {
            sendNotification(member.getEmail(), message, NotificationType.NEW_RANK_AVAILABLE);
        }
    }



    public void sendGlobalRanksAvailable(String email, String tournamentTitle) {
        var message = "\n" +
                "Final ranking of the tournament '%s' now available, hurry and see it!!!"
                        .formatted(tournamentTitle);
        sendNotification(email, message, NotificationType.NEW_RANK_AVAILABLE);
    }

    public void sendSuccessfulBattleEnrollment(User subscriber, Battle battle) {
        var message = "You have successfully enrolled in the '%s' battle".formatted(battle.getTitle());
        var email = subscriber.getEmail();
        sendNotification(email, message, NotificationType.SUCCESSFUL_BATTLE_ENROLLMENT);
    }

    public void sendSuccessfulTournamentSubscription(User subscriber, Tournament tournament) {
        var message = "You have successfully subscribed to the '%s' tournament. You will receive notifications of upcoming battles!".formatted(tournament.getTitle());
        var email = subscriber.getEmail();
        sendNotification(email, message, NotificationType.SUCCESSFUL_TOURNAMENT_SUBSCRIPTION);
    }

    public void sendManualEvaluationRequired(Tournament tournament, String battleTitle) {
        var message = "\n" +
                "The manual evaluation is required in the battle '%s' of the tournament '%s'!"
                        .formatted(tournament.getTitle(), battleTitle);

        for (var manager : tournament.getEducators()) {
            sendNotification(manager.getEmail(), message, NotificationType.MANUAL_EVALUATION_REQUIRED);
        }
    }

    /**
     * Sends a notification to a user
     * @param user_email User email used for identification and for sending an email
     * @param message body of the notification
     */
    public void sendNotification(String user_email, String message, NotificationType type){
        var notification = Notification.builder()
                .id(ObjectId.get().toString())
                .type(type)
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

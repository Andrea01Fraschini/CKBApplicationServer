package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender emailSender;
    private final UserRepository userRepository;
    private final UserService userService;

    public void sendTournamentCreationNotifications(Tournament tournament){
        String message = "A tournament titled '%s' has been created by '%s'. Go ahead and join it!"
                .formatted(tournament.getTitle(), tournament.getEducators().get(0).getUsername());

        for(var email : userRepository.getAllStudentsEmails()){
            sendNotification(email.getEmail(), message);
        }
    }

    public void sendNotification(String user_email, String message){
        var notification = Notification.builder()
                .message(message)
                .is_closed(false)
                .creation_date(new Date(System.currentTimeMillis()))
                .build();

        userService.addNotification(user_email, notification);
        sendEmail(user_email, "New notification from CodeKataBattle",
                "You received a notification from code kata battle:\n\n" +
                        "'%s'".formatted(message));
    }

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

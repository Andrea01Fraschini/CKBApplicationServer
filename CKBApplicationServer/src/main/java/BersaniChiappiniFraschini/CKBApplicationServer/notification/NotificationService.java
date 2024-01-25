package BersaniChiappiniFraschini.CKBApplicationServer.notification;

import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender emailSender;
    public void sendTournamentCreationNotification(Tournament tournament){

    }


    public void testEmailSend(){
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom("noreply@codekattabattle.com");
        email.setTo("ormalend01@gmail.com");
        email.setSubject("BELANDI BESUGHI");
        email.setText("Ciao! Mi chiamo CODEKATABATTLE e volevo dirti\nAAAAAAAAAAAAAAAAAAAAAAAAAA\n\nDistinti saluti");
        emailSender.send(email);
    }
}

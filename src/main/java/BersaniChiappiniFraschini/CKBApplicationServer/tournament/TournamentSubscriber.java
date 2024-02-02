package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/*
 * This record represents a simplified view of a user in the context of a tournament.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@Builder
public class TournamentSubscriber {
    private String id;
    private String username;
    private String email;
    private int score;

    public TournamentSubscriber(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.score = 0;
    }
}

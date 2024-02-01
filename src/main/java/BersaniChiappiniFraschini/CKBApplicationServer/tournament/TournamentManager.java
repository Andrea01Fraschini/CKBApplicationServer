package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class TournamentManager {
    private String id;
    private String username;

    private String email;

    public TournamentManager(User educator) {
        this.id = educator.getId();
        this.username = educator.getUsername();
        this.email = educator.getEmail();
    }
}

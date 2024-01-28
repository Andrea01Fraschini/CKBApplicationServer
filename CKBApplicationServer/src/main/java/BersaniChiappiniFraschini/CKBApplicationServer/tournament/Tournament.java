package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Tournament {
    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    private boolean is_open;
    private List<TournamentSubscriber> subscribed_users;
    private List<TournamentManager> educators;
    private List<PendingInvite> pending_invites;
    private List<Battle> battles;
    private Date subscription_deadline;

    @Override
    public String toString() {
        return "Tournament{" +
                "id='" + id + '\'' +
                ",\n title='" + title + '\'' +
                ",\n is_open=" + is_open +
                ",\n subscribed_users=" + subscribed_users +
                ",\n educators=" + educators +
                ",\n pending_invites=" + pending_invites +
                ",\n battles=" + battles +
                ",\n subscription_deadline=" + subscription_deadline +
                '}';
    }
}

package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.Tournament;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Document
public class Invite { // Work In progress
    @Id
    private String id;
    @Indexed
    private User sender;
    @Indexed
    private User receiver;
    private Tournament tournament;
    private Group group;
}

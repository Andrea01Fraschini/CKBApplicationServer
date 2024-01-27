package BersaniChiappiniFraschini.CKBApplicationServer.invite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Invite { // Work In progress
    @Id
    private String id;
    @Indexed
    private String sender;
    @Indexed
    private String receiver;
    private String tournament_id;
    private String group_id;
}

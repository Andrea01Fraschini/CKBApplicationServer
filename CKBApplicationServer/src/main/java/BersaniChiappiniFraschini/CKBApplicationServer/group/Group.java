package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Group {
    @Id
    private String id; //Work in Progress
    private GroupMember leader;
    private List<GroupMember> members;
    private List<PendingInvite> pending_invites;
    private Map<String, Integer> scores;
    private String repository;
}

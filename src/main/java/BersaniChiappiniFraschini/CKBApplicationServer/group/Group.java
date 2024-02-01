package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Group {
    @Id
    private String id;
    private GroupMember leader;
    private List<GroupMember> members;
    private List<PendingInvite> pending_invites;
    @Builder.Default
    private List<ScoringParameter> scoringParameters = List.of();
    @Builder.Default
    private List<TestResults> testResults = List.of();
    private String repository;
    private String API_Token;
    private Date last_update;
    private int total_score;
    private boolean done_manual_evaluation;

    public record ScoringParameter (
        String name,
        int score
    ) {}

    public record TestResults (
            String testName,
            boolean status
    ) {}
}

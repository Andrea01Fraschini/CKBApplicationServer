package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
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
    private User leader;
    private List<User> members;
    private List<User> pending_invites;
    private Map<String, Integer> scores;
    private String repository;
}

package BersaniChiappiniFraschini.CKBApplicationServer.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {
    private String tournament_id;

    // dubbioso dipende da come viene gestito lato front-end
    private String group_id;
    private String repository;

    private String group_leader;

}

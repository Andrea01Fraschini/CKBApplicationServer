package BersaniChiappiniFraschini.CKBApplicationServer.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetGroupRepositoryRequest {
    private String tournament_title;
    private String group_id;
    private String repository;
    private String group_leader;
}

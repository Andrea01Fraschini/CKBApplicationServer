package BersaniChiappiniFraschini.CKBApplicationServer.tournament;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentCreationRequest {
    private String title;
    private Date subscription_deadline;
    private List<String> invited_managers;
}

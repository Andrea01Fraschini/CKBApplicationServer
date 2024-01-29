package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;

import BersaniChiappiniFraschini.CKBApplicationServer.battle.Battle;
import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
public class SupportClassInfoStudent {
    private String tournamentTitle;
    private String title;
    private Date submission_deadline;
    private Group groups;
}
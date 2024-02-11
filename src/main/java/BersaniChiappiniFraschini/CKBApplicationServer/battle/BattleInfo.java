package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to send information to the frontend in the format shown below.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BattleInfo {
    private String title;
    private String description;
    private String language;
    private int min_group_size;
    private int max_group_size;
    private String repository;
    private Date enrollment_deadline;
    private Date submission_deadline;
    private boolean manual_evaluation;
    private List<EvalParameter> evaluation_parameters;
    private List<LeaderboardEntry> leaderboard;
    private Optional<Group> group;
    private List<Group> groups;
}

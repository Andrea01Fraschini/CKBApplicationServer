package BersaniChiappiniFraschini.CKBApplicationServer.battle;

import BersaniChiappiniFraschini.CKBApplicationServer.group.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Battle {
    @Id
    private String id; //Work in Progress
    @Indexed
    private String title;
    private int min_group_size;
    private int max_group_size;
    private String description;
    private String repository;
    private Date enrollment_deadline;
    private Date submission_deadline;
    private boolean manual_evaluation;
    private List<Group> groups;
    private List<EvalParameter> evaluation_parameters;

    //TODO test
}

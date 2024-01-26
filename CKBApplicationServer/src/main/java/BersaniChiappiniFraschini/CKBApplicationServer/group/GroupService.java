package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final MongoTemplate mongoTemplate;

    public void inviteStudent(String tournament_id, String battle_id, String group_id, User receiver) {
        Query query = new Query(Criteria.where("_id").is(tournament_id));
        var update = new Update().push("battles.$[battle].groups.$[group].pending_invites", receiver)
                .filterArray(Criteria.where("battle._id").is(battle_id))
                .filterArray(Criteria.where("group._id").is(group_id));

        mongoTemplate.updateFirst(query, update, "tournament");
    }
}

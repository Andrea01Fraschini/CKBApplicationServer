package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/*
 * This service acts similarly to the GroupService as it handles the invitation as manager to a tournament.
 */
@Service
@RequiredArgsConstructor
public class ManagersService {
    private final MongoTemplate mongoTemplate;

    public void inviteManager(String tournament_title, User receiver) {
        Query query = new Query(Criteria.where("title").is(tournament_title));
        var update = new Update().push("pending_invites", receiver);

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    public void acceptManagerInvite(String tournament_id, User user) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(tournament_id)));
        var update = new Update()
                .push("educators", user)
                .pull("pending_invites", Query.query(Criteria.where("_id").is(new ObjectId(user.getId()))));

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    public void rejectManagerInvite(String tournament_id, User user) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(tournament_id)));
        var update = new Update()
                .pull("pending_invites", Query.query(Criteria.where("_id").is(new ObjectId(user.getId()))));

        mongoTemplate.updateFirst(query, update, "tournament");
    }
}

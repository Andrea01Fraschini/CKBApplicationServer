package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.notification.NotificationService;
import BersaniChiappiniFraschini.CKBApplicationServer.tournament.TournamentRepository;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final MongoTemplate mongoTemplate;

    public void inviteStudent(String tournament_title, String battle_title, String group_id, User receiver) {
        Query query = new Query(Criteria.where("title").is(tournament_title));
        var update = new Update()
                .push("battles.$[battle].groups.$[group].pending_invites", receiver)
                .filterArray(Criteria.where("battle.title").is(battle_title))
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    public void acceptGroupInvite(String tournament_id, String group_id, User user) {
        Query query = new Query(Criteria.where("_id")
                .is(new ObjectId(tournament_id))
                .and("battles.groups._id").is(new ObjectId(group_id)));
        var update = new Update()
                .push("battles.$.groups.$[group].members", user)
                .pull("battles.$.groups.$[group].pending_invites", Query.query(Criteria.where("_id").is(new ObjectId(user.getId()))))
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    public void rejectGroupInvite(String tournament_id, String group_id, User user) {
        Query query = new Query(Criteria.where("_id")
                .is(new ObjectId(tournament_id))
                .and("battles.groups._id").is(new ObjectId(group_id)));
        var update = new Update()
                .pull("battles.$.groups.$[group].pending_invites", Query.query(Criteria.where("_id").is(new ObjectId(user.getId()))))
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");
    }
}

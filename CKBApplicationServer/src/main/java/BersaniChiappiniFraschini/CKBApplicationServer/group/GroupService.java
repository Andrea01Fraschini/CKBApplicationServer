package BersaniChiappiniFraschini.CKBApplicationServer.group;

import BersaniChiappiniFraschini.CKBApplicationServer.genericResponses.PostResponse;
import BersaniChiappiniFraschini.CKBApplicationServer.invite.PendingInvite;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final MongoTemplate mongoTemplate;

    public void inviteStudent(String tournament_title, String battle_title, String group_id, User receiver) {
        Query query = new Query(Criteria.where("title").is(tournament_title));
        var update = new Update()
                .push("battles.$[battle].groups.$[group].pending_invites", new PendingInvite(receiver))
                .filterArray(Criteria.where("battle.title").is(battle_title))
                .filterArray(Criteria.where("group._id").is(new ObjectId(group_id)));

        mongoTemplate.updateFirst(query, update, "tournament");
    }

    public void acceptGroupInvite(String tournament_id, String group_id, User user) {
        Query query = new Query(Criteria.where("_id")
                .is(new ObjectId(tournament_id))
                .and("battles.groups._id").is(new ObjectId(group_id)));
        var update = new Update()
                .push("battles.$.groups.$[group].members", new GroupMember(user))
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

    public ResponseEntity<PostResponse> setRepository(GroupRequest groupRequest) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        AccountType accountType = AccountType.valueOf(auth.getAuthorities().stream().toList().get(0).toString());
        if (accountType != AccountType.STUDENT) {
            var res = new PostResponse("Cannot set repository of the group");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        var username = auth.getName();

        if(!username.equals(groupRequest.getGroup_leader())){
            var res = new PostResponse("Only leader of the group can modify the repository");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }

        Query query = new Query(Criteria.where("_id")
                .is(new ObjectId(groupRequest.getTournament_id()))
                .and("battles.groups._id").is(new ObjectId(groupRequest.getGroup_id())));

        var update = new Update()
                .set("battles.$.groups.$[group].repository", groupRequest.getRepository())
                .filterArray(Criteria.where("group._id").is(new ObjectId(groupRequest.getGroup_id())));

        mongoTemplate.updateFirst(query, update, "tournament");

        var res = new PostResponse("OK");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
    }
}

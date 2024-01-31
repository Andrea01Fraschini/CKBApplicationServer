package BersaniChiappiniFraschini.CKBApplicationServer.search;

import BersaniChiappiniFraschini.CKBApplicationServer.dashboard.CardInfo;
import BersaniChiappiniFraschini.CKBApplicationServer.dashboard.CardInfoEducator;
import lombok.Data;

import java.util.Date;
import java.util.List;

public record TournamentInfo (
    String tournament_title,
    int subscribed_students_count,
    int number_of_battles,
    Date subscription_deadline,
    List<Educator> educators,
    boolean is_open
) implements CardInfo {
    public record Educator(String username/*, String profile_img_url*/){}
}

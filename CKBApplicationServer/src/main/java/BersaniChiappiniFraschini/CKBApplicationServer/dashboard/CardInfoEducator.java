package BersaniChiappiniFraschini.CKBApplicationServer.dashboard;


import java.util.Date;
import java.util.List;

public record CardInfoEducator(
        String tournament_title,
        int subscribed_students_count,
        int number_of_battles,
        Date subscription_deadline,
        List<Educator> educators

) implements CardInfo{
    public record Educator(String username/*, String profile_img_url*/){}
}
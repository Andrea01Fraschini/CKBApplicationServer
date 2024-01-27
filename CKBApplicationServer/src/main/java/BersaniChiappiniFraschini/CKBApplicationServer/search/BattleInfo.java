package BersaniChiappiniFraschini.CKBApplicationServer.search;

import java.util.Date;

public record BattleInfo(
        String battle_title,
        boolean status,
        Date enrollment_deadline,
        int number_enrolled_groups

){

}

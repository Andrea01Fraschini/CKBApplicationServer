package BersaniChiappiniFraschini.CKBApplicationServer.search;

import java.util.Date;

public record BattleInfo(
        String tournament_title,
        String battle_title,
        boolean is_open,
        Date enrollment_deadline,
        int enrolled_groups
){ }

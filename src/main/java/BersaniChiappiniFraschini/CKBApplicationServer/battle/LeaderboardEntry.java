package BersaniChiappiniFraschini.CKBApplicationServer.battle;

/**
 * This record is used to send data to the frontend to compose the leaderboards.
 * @param name display name on the leaderboard.
 * @param score score to display.
 */
public record LeaderboardEntry(
        String name,
        int score
) {
}

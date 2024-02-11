package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Class responsible for converting the output of a Quality analyzer to a numeric score.
 */
public class QualityScoreProcessor {
    private static final NavigableMap<Integer, Integer> scoresMap = new TreeMap<>();

    public QualityScoreProcessor() {
        // The values are arbitrary and may be tuned to the desired ranges of scores.
        scoresMap.put(0, 100); // 0 violations = 100 points
        scoresMap.put(1, 75); // 1 - 20 violations = 75 points
        scoresMap.put(21, 50); // 21 - 50 violations = 50 points
        scoresMap.put(51, 25); // 51 - 100 violations = 25 points
        scoresMap.put(101, 0); // 100 - ∞ violations = 0 points
    }

    /**
     * Computes the score to assign given a number of violations.
     * @param violationsCount number of violations resulted from the static analysis
     * @return the score associated with the number of violations.
     */
    public int computeScore(int violationsCount) {
        Map.Entry<Integer, Integer> entry = scoresMap.floorEntry(violationsCount);
        return entry.getValue();
    }
}

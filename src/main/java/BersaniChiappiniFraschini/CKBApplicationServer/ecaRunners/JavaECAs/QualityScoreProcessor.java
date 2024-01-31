package BersaniChiappiniFraschini.CKBApplicationServer.ecaRunners.JavaECAs;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class QualityScoreProcessor {
    private static final NavigableMap<Integer, Integer> scoresMap = new TreeMap<>();

    public QualityScoreProcessor() {
        scoresMap.put(0, 100);
        scoresMap.put(1, 75);
        scoresMap.put(21, 50);
        scoresMap.put(51, 25);
        scoresMap.put(101, 0);
    }

    public int computeScore(int violationsCount) {
        Map.Entry<Integer, Integer> entry = scoresMap.floorEntry(violationsCount);
        return entry.getValue();
    }
}

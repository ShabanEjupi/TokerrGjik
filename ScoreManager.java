import java.util.prefs.Preferences;

/**
 * Local scoring system with lightweight persistence.
 * - +3 points when forming a mill
 * - +5 points when removing an opponent piece
 * - +20 points for a win
 */
public class ScoreManager {
    private static final String PREF_NODE = "toker_gjik_scores";
    private static final String P1_SCORE = "p1_score";
    private static final String P2_SCORE = "p2_score";
    private static final String P1_WINS = "p1_wins";
    private static final String P2_WINS = "p2_wins";

    private final Preferences prefs;

    public ScoreManager() {
        prefs = Preferences.userRoot().node(PREF_NODE);
    }

    public void addMillPoints(int player) { addPoints(player, 3); }
    public void addCapturePoints(int player) { addPoints(player, 5); }
    public void addWinPoints(int player) {
        addPoints(player, 20);
        if (player == 1) {
            prefs.putInt(P1_WINS, getWins(1) + 1);
        } else {
            prefs.putInt(P2_WINS, getWins(2) + 1);
        }
    }

    public void addPoints(int player, int points) {
        if (player == 1) {
            prefs.putInt(P1_SCORE, getScore(1) + points);
        } else {
            prefs.putInt(P2_SCORE, getScore(2) + points);
        }
    }

    public int getScore(int player) {
        return player == 1 ? prefs.getInt(P1_SCORE, 0) : prefs.getInt(P2_SCORE, 0);
    }

    public int getWins(int player) {
        return player == 1 ? prefs.getInt(P1_WINS, 0) : prefs.getInt(P2_WINS, 0);
    }

    public void reset() {
        prefs.putInt(P1_SCORE, 0);
        prefs.putInt(P2_SCORE, 0);
        prefs.putInt(P1_WINS, 0);
        prefs.putInt(P2_WINS, 0);
    }

    public String formatScoreLine() {
        return String.format("⭐ Pikët / Score: P1(%d) W:%d  |  P2(%d) W:%d", getScore(1), getWins(1), getScore(2), getWins(2));
    }
}

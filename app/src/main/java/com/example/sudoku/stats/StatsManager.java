package com.example.sudoku.stats;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.Locale;

import java.util.HashMap;
import java.util.Map;

public class StatsManager {
    public interface SummaryCallback {
        void onSummary(String summary);
    }

    private static final String COLLECTION = "users";
    private static final String DIFFICULTY_EASY = "EINFACH";
    private static final String DIFFICULTY_MEDIUM = "MITTEL";
    private static final String DIFFICULTY_HARD = "SCHWER";
    private static final String[] DIFFICULTIES = {DIFFICULTY_EASY, DIFFICULTY_MEDIUM, DIFFICULTY_HARD};

    private final FirebaseFirestore firestore;

    public StatsManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void recordGame(String uid, String difficulty, boolean win, int seconds)
    {
        DocumentReference ref = firestore.collection(COLLECTION)
                .document(uid);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);
            long played = getLong(snapshot, "played");
            long wins = getLong(snapshot, "wins");
            long totalSeconds = getLong(snapshot, "totalSeconds");
            long bestSeconds = getLong(snapshot, "bestSeconds");
            String diffKey = difficultyKey(difficulty);
            long diffPlayed = getLong(snapshot, key("played", diffKey));
            long diffWins = getLong(snapshot, key("wins", diffKey));
            long diffTotalSeconds = getLong(snapshot, key("totalSeconds", diffKey));
            long diffBestSeconds = getLong(snapshot, key("bestSeconds", diffKey));

            long newPlayed = played + 1;
            long newWins = win ? (wins + 1) : wins;
            long newTotalSeconds = totalSeconds;
            long newBestSeconds = bestSeconds;

            if (seconds > 0) {
                newTotalSeconds = totalSeconds + seconds;
                if (bestSeconds == 0 || seconds < bestSeconds) {
                    newBestSeconds = seconds;
                }
            }

            long avgSeconds = newPlayed > 0 && newTotalSeconds > 0 ? newTotalSeconds / newPlayed : 0;

            long newDiffPlayed = diffPlayed + 1;
            long newDiffWins = win ? (diffWins + 1) : diffWins;
            long newDiffTotalSeconds = diffTotalSeconds;
            long newDiffBestSeconds = diffBestSeconds;

            if (seconds > 0) {
                newDiffTotalSeconds = diffTotalSeconds + seconds;
                if (diffBestSeconds == 0 || seconds < diffBestSeconds) {
                    newDiffBestSeconds = seconds;
                }
            }

            long diffAvgSeconds = newDiffPlayed > 0 && newDiffTotalSeconds > 0 ? newDiffTotalSeconds / newDiffPlayed : 0;

            Map<String, Object> payload = new HashMap<>();
            payload.put("played", newPlayed);
            payload.put("wins", newWins);
            payload.put("totalSeconds", newTotalSeconds);
            payload.put("bestSeconds", newBestSeconds);
            payload.put("avgTime", avgSeconds);
            payload.put("bestTime", newBestSeconds);
            payload.put(key("played", diffKey), newDiffPlayed);
            payload.put(key("wins", diffKey), newDiffWins);
            payload.put(key("totalSeconds", diffKey), newDiffTotalSeconds);
            payload.put(key("bestSeconds", diffKey), newDiffBestSeconds);
            payload.put(key("avgTime", diffKey), diffAvgSeconds);
            payload.put(key("bestTime", diffKey), newDiffBestSeconds);
            transaction.set(ref, payload, SetOptions.merge());
            return null;
        });
    }

    public void getSummary(String uid, SummaryCallback cb) {
        firestore.collection(COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    long played = getLong(doc, "played");
                    long wins = getLong(doc, "wins");
                    long totalSeconds = getLong(doc, "totalSeconds");
                    long bestSeconds = getLong(doc, "bestSeconds");
                    long avgSeconds = getLong(doc, "avgTime");
                    if (avgSeconds == 0 && played > 0 && totalSeconds > 0) {
                        avgSeconds = totalSeconds / played;
                    }
                    String avgTime = avgSeconds > 0 ? formatTime((int) avgSeconds) : "--:--";
                    String bestTime = bestSeconds > 0 ? formatTime((int) bestSeconds) : "--:--";
                    StringBuilder summary = new StringBuilder("Gesamt: gespielt " + played
                            + ", gewonnen " + wins
                            + ", Ø Zeit " + avgTime
                            + ", Bestzeit " + bestTime);
                    appendDifficultySummary(summary, doc);
                    cb.onSummary(summary.toString());
                })
                .addOnFailureListener(e -> cb.onSummary("Fehler beim Laden der Statistik."));
    }

    private long getLong(DocumentSnapshot doc, String key) {
        if (doc == null) return 0;
        Long value = doc.getLong(key);
        return value != null ? value : 0;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }

    private void appendDifficultySummary(StringBuilder builder, DocumentSnapshot doc) {
        for (String difficulty : DIFFICULTIES) {
            String diffKey = difficultyKey(difficulty);
            long played = getLong(doc, key("played", diffKey));
            if (played == 0) {
                continue;
            }
            long wins = getLong(doc, key("wins", diffKey));
            long totalSeconds = getLong(doc, key("totalSeconds", diffKey));
            long bestSeconds = getLong(doc, key("bestSeconds", diffKey));
            long avgSeconds = getLong(doc, key("avgTime", diffKey));
            if (avgSeconds == 0 && played > 0 && totalSeconds > 0) {
                avgSeconds = totalSeconds / played;
            }
            String avgTime = avgSeconds > 0 ? formatTime((int) avgSeconds) : "--:--";
            String bestTime = bestSeconds > 0 ? formatTime((int) bestSeconds) : "--:--";
            builder.append("\n")
                    .append(difficulty)
                    .append(": gespielt ")
                    .append(played)
                    .append(", gewonnen ")
                    .append(wins)
                    .append(", Ø Zeit ")
                    .append(avgTime)
                    .append(", Bestzeit ")
                    .append(bestTime);
        }
    }

    private String difficultyKey(String difficulty) {
        if (difficulty == null) {
            return "unknown";
        }
        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case DIFFICULTY_EASY:
                return "easy";
            case DIFFICULTY_MEDIUM:
                return "medium";
            case DIFFICULTY_HARD:
                return "hard";
            default:
                return "unknown";
        }
    }

    private String key(String base, String diffKey) {
        return base + "_" + diffKey;
    }
}
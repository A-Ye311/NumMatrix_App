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

    private final FirebaseFirestore firestore;

    public StatsManager() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void recordGame(String uid, boolean win, int seconds)
    {
        DocumentReference ref = firestore.collection(COLLECTION)
                .document(uid);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);
            long played = getLong(snapshot, "played");
            long wins = getLong(snapshot, "wins");
            long totalSeconds = getLong(snapshot, "totalSeconds");
            long bestSeconds = getLong(snapshot, "bestSeconds");

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

            Map<String, Object> payload = new HashMap<>();
            payload.put("played", newPlayed);
            payload.put("wins", newWins);
            payload.put("totalSeconds", newTotalSeconds);
            payload.put("bestSeconds", newBestSeconds);
            payload.put("avgTime", avgSeconds);
            payload.put("bestTime", newBestSeconds);
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
                    String summary = "Gesamt: gespielt " + played
                            + ", gewonnen " + wins
                            + ", Ø Zeit " + avgTime
                            + ", Bestzeit " + bestTime;
                    cb.onSummary(summary);
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
}

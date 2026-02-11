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
    private static final String[] DIFFICULTIES = {"EINFACH", "MITTEL", "SCHWER"};

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void recordGame(String uid, String difficulty, boolean win, int seconds) {
        DocumentReference ref = firestore.collection(COLLECTION).document(uid);
        String diffKey = difficultyKey(difficulty);

        firestore.runTransaction(tx -> {
            DocumentSnapshot doc = tx.get(ref);

            Map<String, Object> payload = new HashMap<>();
            updateBlock(doc, payload, "", win, seconds);                 // Gesamt
            updateBlock(doc, payload, "_" + diffKey, win, seconds);      // Pro Schwierigkeit

            tx.set(ref, payload, SetOptions.merge());
            return null;
        });
    }

    // suffix = "" (gesamt) oder "_easy/_medium/_hard"
    private void updateBlock(DocumentSnapshot doc, Map<String, Object> out, String suffix,
                             boolean win, int seconds) {

        long played = getLong(doc, "played" + suffix);
        long wins = getLong(doc, "wins" + suffix);
        long total = getLong(doc, "totalSeconds" + suffix);
        long best = getLong(doc, "bestSeconds" + suffix);

        played++;
        if (win) wins++;

        if (seconds > 0) {
            total += seconds;
            if (best == 0 || seconds < best) best = seconds;
        }

        out.put("played" + suffix, played);
        out.put("wins" + suffix, wins);
        out.put("totalSeconds" + suffix, total);
        out.put("bestSeconds" + suffix, best);
        // avgTime speichern wir nicht, wird beim Anzeigen berechnet
    }

    public void getSummary(String uid, SummaryCallback cb) {
        firestore.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(doc -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(summaryLine(doc, "Gesamt", ""));

                    for (String d : DIFFICULTIES) {
                        String k = "_" + difficultyKey(d);
                        long played = getLong(doc, "played" + k);
                        if (played > 0) {
                            sb.append("\n").append(summaryLine(doc, d, k));
                        }
                    }
                    cb.onSummary(sb.toString());
                })
                .addOnFailureListener(e -> cb.onSummary("Fehler beim Laden der Statistik."));
    }

    private String summaryLine(DocumentSnapshot doc, String label, String suffix) {
        long played = getLong(doc, "played" + suffix);
        long wins = getLong(doc, "wins" + suffix);
        long total = getLong(doc, "totalSeconds" + suffix);
        long best = getLong(doc, "bestSeconds" + suffix);

        long avg = (played > 0 && total > 0) ? (total / played) : 0;

        String avgTime = avg > 0 ? formatTime((int) avg) : "--:--";
        String bestTime = best > 0 ? formatTime((int) best) : "--:--";

        return label + ": gespielt " + played
                + ", gewonnen " + wins
                + ", Ø Zeit " + avgTime
                + ", Bestzeit " + bestTime;
    }

    private long getLong(DocumentSnapshot doc, String key) {
        if (doc == null) return 0;
        Long v = doc.getLong(key);
        return v != null ? v : 0;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }

    private String difficultyKey(String difficulty) {
        if (difficulty == null) return "unknown";
        String s = difficulty.trim().toUpperCase(Locale.ROOT);
        if (s.equals("EINFACH")) return "easy";
        if (s.equals("MITTEL")) return "medium";
        if (s.equals("SCHWER")) return "hard";
        return "unknown";
    }
}
package com.example.sudoku.stats;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * Speichert und lädt Spielstatistiken aus Firestore.
 */
public class StatsManager {
    /** Callback für erfolgreich geladene Statistik-Zeilen. */
    public interface SummaryCallback {
        void onSummary(List<StatRow> rows);
    }
    /** Callback für einen Ladefehler. */
    public interface ErrorCallback {
        void onError();
    }
    /**
     * Eine Zeile für die Statistik-Anzeige.
     */
    public static class StatRow {
        public final String label;
        public final long played;
        public final long wins;
        public final String avgTime;
        public final String bestTime;
        /** Speichert alle Werte, die in einer Tabellenzeile gezeigt werden. */
        public StatRow(String label, long played, long wins, String avgTime, String bestTime) {
            this.label = label;
            this.played = played;
            this.wins = wins;
            this.avgTime = avgTime;
            this.bestTime = bestTime;
        }
    }

    private static final String COLLECTION = "users";
    private static final String[] DIFFICULTIES = {"EINFACH", "MITTEL", "SCHWER"};

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    /**
     * Speichert das Ergebnis eines Spiels für Gesamtstatistik und Schwierigkeit.
     */
    public void recordGame(String uid, String difficulty, boolean win, int seconds) {
        DocumentReference ref = firestore.collection(COLLECTION).document(uid);
        String diffKey = difficultyKey(difficulty);

        firestore.runTransaction(tx -> {
            DocumentSnapshot doc = tx.get(ref);

            Map<String, Object> payload = new HashMap<>();
            updateBlock(doc, payload, "", win, seconds);
            updateBlock(doc, payload, "_" + diffKey, win, seconds);

            tx.set(ref, payload, SetOptions.merge());
            return null;
        });
    }
    /**
     * Aktualisiert einen Statistik-Block mit neuen Werten.
     */
    private void updateBlock(DocumentSnapshot doc, Map<String, Object> out, String suffix,
                             boolean win, int seconds) {

        long played = getLong(doc, "played" + suffix);
        long wins = getLong(doc, "wins" + suffix);
        long total = getLong(doc, "totalSeconds" + suffix);
        long best = getLong(doc, "bestSeconds" + suffix);

        played++;
        if (win){
            wins++;
        }

        if (seconds > 0) {
            total += seconds;
            if (win && (best == 0 || seconds < best)){
                best = seconds;
            }
        }

        out.put("played" + suffix, played);
        out.put("wins" + suffix, wins);
        out.put("totalSeconds" + suffix, total);
        out.put("bestSeconds" + suffix, best);
    }
    /**
     * Lädt eine Zusammenfassung für Gesamt und alle Schwierigkeitsstufen.
     */
    public void getSummary(String uid, SummaryCallback onSuccess, ErrorCallback onError) {
        firestore.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(doc -> {
                    List<StatRow> rows = new ArrayList<>();
                    rows.add(summaryRow(doc, "Gesamt", ""));

                    for (String d : DIFFICULTIES) {
                        String k = "_" + difficultyKey(d);
                        rows.add(summaryRow(doc, d, k));
                    }

                    onSuccess.onSummary(rows);
                })
                .addOnFailureListener(e -> onError.onError());
    }
    /**
     * Baut eine Zeile mit zusammengefassten Werten.
     */
    private StatRow summaryRow(DocumentSnapshot doc, String label, String suffix) {
        long played = getLong(doc, "played" + suffix);
        long wins = getLong(doc, "wins" + suffix);
        long total = getLong(doc, "totalSeconds" + suffix);
        long best = getLong(doc, "bestSeconds" + suffix);

        long avg = (played > 0 && total > 0) ? (total / played) : 0;

        String avgTime = avg > 0 ? formatTime((int) avg) : formatTime(0);
        String bestTime = (wins > 0 && best > 0) ? formatTime((int) best) : formatTime(0);

        return new StatRow(label, played, wins, avgTime, bestTime);
    }
    /** Liest eine Zahl aus Firestore. Fehlt der Wert, wird 0 genutzt. */
    private long getLong(DocumentSnapshot doc, String key) {
        if (doc == null) {
            return 0;
        }
        Long v = doc.getLong(key);
        return v != null ? v : 0;
    }
    /** Wandelt Sekunden in das Format mm:ss um. */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
    }
    /** Übersetzt die Schwierigkeit in einen kurzen Firestore-Schlüssel. */
    private String difficultyKey(String difficulty) {
        if (difficulty == null) {
            return "unknown";
        }

        String normalized = difficulty.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "EINFACH":
                return "easy";
            case "MITTEL":
                return "medium";
            case "SCHWER":
                return "hard";
            default:
                return "unknown";
        }
    }
}
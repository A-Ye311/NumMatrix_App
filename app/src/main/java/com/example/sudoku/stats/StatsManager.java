package com.example.sudoku.stats;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;import java.util.HashMap;
import java.util.Map;

public class StatsManager {
    private static final String PREF = "sudoku_stats";
    private static final String KEY = "stats_json";
    private final FirebaseFirestore firestore;
    private final SharedPreferences sp;

    public StatsManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();
    }

    private JSONObject load() {
        try { return new JSONObject(sp.getString(KEY, "{}")); }
        catch (JSONException e) { return new JSONObject(); }
    }

    private void save(JSONObject obj) {
        sp.edit().putString(KEY, obj.toString()).apply();
    }
    private void syncStats(String email, String difficulty, JSONObject entry) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("played", entry.optInt("played", 0));
        payload.put("wins", entry.optInt("wins", 0));
        payload.put("totalSeconds", entry.optInt("totalSeconds", 0));
        payload.put("bestSeconds", entry.optInt("bestSeconds", 0));
        firestore.collection("users")
                .document(email)
                .collection("stats")
                .document(difficulty)
                .set(payload, SetOptions.merge());
    }
    private String userKey(String email, String difficulty) {
        return email + "::" + difficulty;
    }

    public void recordGame(String email, String difficulty, boolean win) {
        recordGame(email, difficulty, win, 0);
    }
    @SuppressWarnings("unused")
    public void recordGame(String email, String difficulty, boolean win, int seconds) {
        JSONObject obj = load();
        String k = userKey(email, difficulty);
        try {
            JSONObject entry = obj.has(k) ? obj.getJSONObject(k) : new JSONObject();
            int played = entry.optInt("played", 0);
            int wins = entry.optInt("wins", 0);
            int totalSeconds = entry.optInt("totalSeconds", 0);
            int bestSeconds = entry.optInt("bestSeconds", 0);
            entry.put("played", played + 1);
            entry.put("wins", win ? (wins + 1) : wins);
            if (seconds > 0) {
                entry.put("totalSeconds", totalSeconds + seconds);
                if (bestSeconds == 0 || seconds < bestSeconds) {
                    entry.put("bestSeconds", seconds);
                } else {
                    entry.put("bestSeconds", bestSeconds);
                }
            }
            obj.put(k, entry);
            save(obj);
            syncStats(email, difficulty, entry);
        } catch (JSONException ignored) {}
    }
    public String getSummary(String email) {
        JSONObject obj = load();
        StringBuilder sb = new StringBuilder();
        String[] diffs = {"EINFACH", "MITTEL", "SCHWER"};

        for (String d : diffs) {
            String k = userKey(email, d);
            JSONObject entry = obj.optJSONObject(k);
            int played = entry != null ? entry.optInt("played", 0) : 0;
            int wins = entry != null ? entry.optInt("wins", 0) : 0;
            int totalSeconds = entry != null ? entry.optInt("totalSeconds", 0) : 0;
            int bestSeconds = entry != null ? entry.optInt("bestSeconds", 0) : 0;
            String avgTime = played > 0 && totalSeconds > 0 ? formatTime(totalSeconds / played) : "--:--";
            String bestTime = bestSeconds > 0 ? formatTime(bestSeconds) : "--:--";
            sb.append(d)
                    .append(": gespielt ").append(played)
                    .append(", gewonnen ").append(wins)
                    .append(", Ø Zeit ").append(avgTime)
                    .append(", Bestzeit ").append(bestTime)
                    .append("\n");
            }
            return sb.toString();
    }
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, secs);
    }
}

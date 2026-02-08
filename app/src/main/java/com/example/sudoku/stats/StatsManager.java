package com.example.sudoku.stats;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class StatsManager {
    private static final String PREF = "sudoku_stats";
    private static final String KEY = "stats_json";

    private final SharedPreferences sp;

    public StatsManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private JSONObject load() {
        try { return new JSONObject(sp.getString(KEY, "{}")); }
        catch (JSONException e) { return new JSONObject(); }
    }

    private void save(JSONObject obj) {
        sp.edit().putString(KEY, obj.toString()).apply();
    }

    private String userKey(String email, String difficulty) {
        return email + "::" + difficulty;
    }

    public void recordGame(String email, String difficulty, boolean win) {
        recordGame(email, difficulty, win, 0);
    }

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
        return String.format("%02d:%02d", minutes, secs);
    }
}

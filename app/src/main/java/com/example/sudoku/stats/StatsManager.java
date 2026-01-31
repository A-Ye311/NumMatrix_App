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
        JSONObject obj = load();
        String k = userKey(email, difficulty);
        try {
            JSONObject entry = obj.has(k) ? obj.getJSONObject(k) : new JSONObject();
            int played = entry.optInt("played", 0);
            int wins = entry.optInt("wins", 0);
            entry.put("played", played + 1);
            entry.put("wins", win ? (wins + 1) : wins);
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
            sb.append(d).append(": gespielt ").append(played).append(", gewonnen ").append(wins).append("\n");
        }
        return sb.toString();
    }
}

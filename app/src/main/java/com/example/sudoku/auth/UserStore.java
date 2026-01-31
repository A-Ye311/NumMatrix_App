package com.example.sudoku.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

public class UserStore {
    private static final String PREF = "sudoku_users";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_LOGGED_IN = "logged_in_email";

    private final SharedPreferences sp;

    public UserStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private JSONObject loadUsers() {
        String raw = sp.getString(KEY_USERS, "{}");
        try { return new JSONObject(raw); }
        catch (JSONException e) { return new JSONObject(); }
    }

    private void saveUsers(JSONObject obj) {
        sp.edit().putString(KEY_USERS, obj.toString()).apply();
    }

    public boolean userExists(String email) {
        JSONObject users = loadUsers();
        return users.has(email);
    }

    public boolean register(String email, String password) {
        JSONObject users = loadUsers();
        if (users.has(email)) return false;
        try {
            users.put(email, password); // super simpel (für Uni-Projekt ok)
            saveUsers(users);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean verifyLogin(String email, String password) {
        JSONObject users = loadUsers();
        if (!users.has(email)) return false;
        try {
            return password.equals(users.getString(email));
        } catch (JSONException e) {
            return false;
        }
    }

    public boolean resetPassword(String email, String newPassword) {
        JSONObject users = loadUsers();
        if (!users.has(email)) return false;
        try {
            users.put(email, newPassword);
            saveUsers(users);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void setLoggedIn(String email) {
        sp.edit().putString(KEY_LOGGED_IN, email).apply();
    }

    public String getLoggedInEmail() {
        return sp.getString(KEY_LOGGED_IN, null);
    }

    public void logout() {
        sp.edit().remove(KEY_LOGGED_IN).apply();
    }
}

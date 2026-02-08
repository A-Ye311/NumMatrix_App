package com.example.sudoku.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static final String PREF = "sudoku_users";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_LOGGED_IN = "logged_in_email";
    private final FirebaseFirestore firestore;
    private final SharedPreferences sp;

    public UserStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        firestore = FirebaseFirestore.getInstance();
    }

    private JSONObject loadUsers() {
        String raw = sp.getString(KEY_USERS, "{}");
        try { return new JSONObject(raw); }
        catch (JSONException e) { return new JSONObject(); }
    }

    private void saveUsers(JSONObject obj) {
        sp.edit().putString(KEY_USERS, obj.toString()).apply();
    }
    private void syncUser(String email, String password) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        firestore.collection("users")
                .document(email)
                .set(payload, SetOptions.merge());
    }

    public boolean register(String email, String password) {
        JSONObject users = loadUsers();
        if (users.has(email)) return false;
        try {
            users.put(email, password); // super simpel (für Uni-Projekt ok)
            saveUsers(users);
            syncUser(email, password);
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
            syncUser(email, newPassword);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public void setLoggedIn(String email) {
        if (email == null || email.trim().isEmpty()) {
            logout();
            return;
        }
        sp.edit().putString(KEY_LOGGED_IN, email.trim()).apply();
    }

    public String getLoggedInEmail() {
        String email = sp.getString(KEY_LOGGED_IN, null);
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        JSONObject users = loadUsers();
        return users.has(email) ? email : null;
    }

    public void logout() {
        sp.edit().remove(KEY_LOGGED_IN).apply();
    }
}

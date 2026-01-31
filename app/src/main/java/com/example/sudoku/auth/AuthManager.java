package com.example.sudoku.auth;

import android.content.Context;
import android.util.Patterns;

public class AuthManager {
    private final UserStore store;

    public AuthManager(Context ctx) {
        store = new UserStore(ctx);
    }

    public boolean isLoggedIn() {
        return store.getLoggedInEmail() != null;
    }

    public String currentUserEmail() {
        return store.getLoggedInEmail();
    }

    public Result register(String email, String pw, String pw2) {
        if (!isValidEmail(email)) return Result.fail("E-Mail ungültig");
        if (pw == null || pw.length() < 4) return Result.fail("Passwort zu kurz (min 4)");
        if (!pw.equals(pw2)) return Result.fail("Passwörter stimmen nicht überein");
        if (!store.register(email, pw)) return Result.fail("Account existiert bereits");
        store.setLoggedIn(email);
        return Result.ok();
    }

    public Result login(String email, String pw) {
        if (!isValidEmail(email)) return Result.fail("E-Mail ungültig");
        if (!store.verifyLogin(email, pw)) return Result.fail("Login-Daten falsch");
        store.setLoggedIn(email);
        return Result.ok();
    }

    public Result resetPassword(String email, String newPw) {
        if (!isValidEmail(email)) return Result.fail("E-Mail ungültig");
        if (newPw == null || newPw.length() < 4) return Result.fail("Passwort zu kurz (min 4)");
        if (!store.resetPassword(email, newPw)) return Result.fail("E-Mail nicht gefunden");
        return Result.ok();
    }

    public void logout() {
        store.logout();
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static class Result {
        public final boolean ok;
        public final String message;
        private Result(boolean ok, String message) { this.ok = ok; this.message = message; }
        public static Result ok() { return new Result(true, null); }
        public static Result fail(String msg) { return new Result(false, msg); }
    }
}

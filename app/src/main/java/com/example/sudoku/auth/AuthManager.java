package com.example.sudoku.auth;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class AuthManager {
    private final Context context;
    private final FirebaseAuth auth;

    public AuthManager(Context ctx) {
        context = ctx;
        auth = FirebaseAuth.getInstance();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String currentUserEmail() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
    }

    public void register(String email, String pw, String pw2, Callback cb) {
        if (!isValidEmail(email)) {
            cb.onResult(Result.fail("E-Mail ungültig"));
            return;
        }
        if (pw == null || pw.length() < 4) {
            cb.onResult(Result.fail("Passwort zu kurz (min 4)"));
            return;
        }
        if (!pw.equals(pw2)) {
            cb.onResult(Result.fail("Passwörter stimmen nicht überein"));
            return;
        }
        if (context instanceof Activity) {
            auth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener((Activity) context, task -> handleAuthResult(task, cb, "Registrierung fehlgeschlagen"));
        } else {
            auth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> handleAuthResult(task, cb, "Registrierung fehlgeschlagen"));
        }
    }

    public void login(String email, String pw, Callback cb) {
        if (!isValidEmail(email)) {
            cb.onResult(Result.fail("E-Mail ungültig"));
            return;
        }
        if (context instanceof Activity) {
            auth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener((Activity) context, task -> handleAuthResult(task, cb, "Login-Daten falsch"));
        } else {
            auth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> handleAuthResult(task, cb, "Login-Daten falsch"));
        }
    }

    public void resetPassword(String email, Callback cb) {
        if (!isValidEmail(email)) {
            cb.onResult(Result.fail("E-Mail ungültig"));
            return;
        }
        if (context instanceof Activity) {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener((Activity) context, task -> handleAuthResult(task, cb, "Passwort-Reset fehlgeschlagen"));
        } else {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> handleAuthResult(task, cb, "Passwort-Reset fehlgeschlagen"));
        }
    }

    public void logout() {
        auth.signOut();
    }

    private boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void handleAuthResult(Task<?> task, Callback cb, String fallback) {
        if (task.isSuccessful()) {
            cb.onResult(Result.ok());
        } else {
            cb.onResult(Result.fail(errorMessage(task.getException(), fallback)));
        }
    }

    private String errorMessage(Exception exception, String fallback) {
        if (exception == null || exception.getMessage() == null) {
            return fallback;
        }
        return exception.getMessage();
    }

    public static class Result {
        public final boolean ok;
        public final String message;
        private Result(boolean ok, String message) { this.ok = ok; this.message = message; }
        public static Result ok() { return new Result(true, null); }
        public static Result fail(String msg) { return new Result(false, msg); }
    }

    public interface Callback {
        void onResult(Result result);
    }
}
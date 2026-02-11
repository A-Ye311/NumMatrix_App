package com.example.sudoku.auth;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;

import com.example.sudoku.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null; // if-else
    }

    public String currentUserUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public void register(String email, String pw, String pw2, Callback cb) {
        if (isInvalidEmail(email)) {
            cb.onResult(Result.fail(context.getString(R.string.error_email_invalid)));
            return; // E-Mail falsch
        }
        if (pw == null || pw.length() < 4) {
            cb.onResult(Result.fail(context.getString(R.string.error_password_short)));
            return; // Passwort invalide
        }
        if (!pw.equals(pw2)) {
            cb.onResult(Result.fail(context.getString(R.string.error_password_mismatch)));
            return; // Passwort kein Match
        }
        if (context instanceof Activity) {
            auth.createUserWithEmailAndPassword(email, pw) //Listener an Activity gebunden
                    .addOnCompleteListener((Activity) context, task -> handleRegisterResult(task, cb));
        } else {
            auth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> handleRegisterResult(task, cb)); //handleRegisterResult -> Fehlermeldung
        }
    }

    public void login(String email, String pw, Callback cb) {
        if (isInvalidEmail(email)) {
            cb.onResult(Result.fail(context.getString(R.string.error_email_invalid)));
            return;
        }
        if (context instanceof Activity) {
            auth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener((Activity) context, task -> handleLoginResult(task, cb));
        } else {
            auth.signInWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> handleLoginResult(task, cb));
        }
    }

    public void resetPassword(String email, Callback cb) {
        if (isInvalidEmail(email)) {
            cb.onResult(Result.fail(context.getString(R.string.error_email_invalid)));
            return;
        }
        if (context instanceof Activity) {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener((Activity) context, task -> handleAuthResult(task, cb, R.string.error_reset_failed));
        } else {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> handleAuthResult(task, cb, R.string.error_reset_failed));
        }
    }

    public void logout() {
        auth.signOut();
    }

    private boolean isInvalidEmail(String email) {
        return email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void handleAuthResult(Task<?> task, Callback cb, int fallbackResId) {
        if (task.isSuccessful()) {
            cb.onResult(Result.ok());
        } else {
            cb.onResult(Result.fail(errorMessage(task.getException(), context.getString(fallbackResId))));
        }
    }

    private void handleRegisterResult(Task<?> task, Callback cb) {
        if (!task.isSuccessful()) {
            cb.onResult(Result.fail(errorMessage(task.getException(), context.getString(R.string.error_register_failed))));
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            cb.onResult(Result.fail(context.getString(R.string.error_register_failed)));
            return;
        }

        user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
            auth.signOut();
            if (verificationTask.isSuccessful()) {
                cb.onResult(Result.fail(context.getString(R.string.register_verify_email_sent)));
            } else {
                cb.onResult(Result.fail(errorMessage(verificationTask.getException(), context.getString(R.string.error_register_verify_send_failed))));
            }
        });
    }

    private void handleLoginResult(Task<?> task, Callback cb) {
        if (!task.isSuccessful()) {
            cb.onResult(Result.fail(errorMessage(task.getException(), context.getString(R.string.error_login_failed))));
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && !user.isEmailVerified()) {
            auth.signOut();
            cb.onResult(Result.fail(context.getString(R.string.error_email_not_verified)));
            return;
        }

        cb.onResult(Result.ok());
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

package com.example.sudoku.auth;

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;

import com.example.sudoku.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Kümmert sich um Login, Registrierung und Passwort-Reset. */
public class AuthManager {
    private final Context context;
    private final FirebaseAuth auth;

    /** Erstellt den Manager mit Zugriff auf App-Kontext und Firebase. */
    public AuthManager(Context ctx) {
        context = ctx;
        auth = FirebaseAuth.getInstance();
    }
    /**  Prüft, ob gerade ein Benutzer eingeloggt ist. */
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    /** Liefert die E-Mail des aktuellen Benutzers oder null. */
    public String currentUserEmail() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
    }

    /** Liefert die UID des aktuellen Benutzers oder null. */
    public String currentUserUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    /** Registriert einen neuen Benutzer nach einfacher Prüfung der Eingaben. */
    public void register(String email, String pw, String pw2, Callback cb) {
        if (isInvalidEmail(email)) {
            cb.onResult(Result.fail(context.getString(R.string.error_email_invalid)));
            return;
        }
        if (pw == null || pw.length() < 4) {
            cb.onResult(Result.fail(context.getString(R.string.error_password_short)));
            return;
        }
        if (!pw.equals(pw2)) {
            cb.onResult(Result.fail(context.getString(R.string.error_password_mismatch)));
            return;
        }
        if (context instanceof Activity) {
            auth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener((Activity) context, task -> handleRegisterResult(task, cb));
        } else {
            auth.createUserWithEmailAndPassword(email, pw)
                    .addOnCompleteListener(task -> handleRegisterResult(task, cb));
        }
    }
    /** Meldet einen vorhanden Benutzer an. */
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

    /** Schickt eine E-Mail zum Zurücksetzen des Passworts. */
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
    /** Aktueller Benutzer ausloggen. */
    public void logout() {
        auth.signOut();
    }

    /** Prüft, ob die E-Mail leer oder ungültig ist. */
    private boolean isInvalidEmail(String email) {
        return email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    /** Behandelt einfache Firebase-Ergebnisse wie Passwort-Reset. */
    private void handleAuthResult(Task<?> task, Callback cb, int fallbackResId) {
        if (task.isSuccessful()) {
            cb.onResult(Result.ok());
        } else {
            cb.onResult(Result.fail(errorMessage(task.getException(), context.getString(fallbackResId))));
        }
    }
    /** Behandelt das Ergebnis einer Registrierung und startet die Verifizierungs-Mail. */
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
                cb.onResult(Result.ok(context.getString(R.string.register_verify_email_sent)));
            } else {
                cb.onResult(Result.fail(errorMessage(verificationTask.getException(), context.getString(R.string.error_register_verify_send_failed))));
            }
        });
    }
    /** Behandelt das Ergebnis eines Logins. Nicht verifizierte Benutzer werden wieder abgemeldet. */
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
    /** Liest eine Fehlermeldung aus Firebase oder nutzt einen Standardtext. */
    private String errorMessage(Exception exception, String fallback) {
        if (exception == null || exception.getMessage() == null) {
            return fallback;
        }
        return exception.getMessage();
    }
    /** Einfaches Ergebnisobjekt für Erfolg oder Fehlertext. */
    public static class Result {
        public final boolean ok;
        public final String message;
        private Result(boolean ok, String message) { this.ok = ok; this.message = message; }
        public static Result ok() { return new Result(true, null); }
        public static Result ok(String msg) { return new Result(true, msg); }
        public static Result fail(String msg) { return new Result(false, msg); }
    }
    /** Callback für asynchrone Auth-Ergebnisse. */
    public interface Callback {
        void onResult(Result result);
    }
}

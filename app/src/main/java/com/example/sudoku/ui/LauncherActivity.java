package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.sudoku.auth.AuthManager;

/** Startet die passende erste Seite je nach Login-Status. */
public class LauncherActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthManager auth = new AuthManager(this);
        if (auth.isLoggedIn()) {
            startActivity(new Intent(this, MainMenuActivity.class));
        } else {
            startActivity(new Intent(this, StartActivity.class));
        }
        finish();
    }
}

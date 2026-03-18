package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.sudoku.R;

import com.example.sudoku.auth.AuthManager;

/** Einstellungen mit der Möglichkeit zum Ausloggen. */
public class SettingsActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager auth = new AuthManager(this);

        setContentView(R.layout.activity_settings);

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(v -> {
            auth.logout();
            Intent i = new Intent(this, StartActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}

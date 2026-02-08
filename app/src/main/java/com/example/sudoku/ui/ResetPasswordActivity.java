package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;

public class ResetPasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager auth = new AuthManager(this);
        String email = getIntent().getStringExtra("email");

        // ✅ 1) XML laden (nur einmal!)
        setContentView(R.layout.activity_reset_password);

        // ✅ 2) Views holen
        TextView title = findViewById(R.id.tvTitle);
        TextView msg = findViewById(R.id.tvMsg);

        title.setText("Passwort zurücksetzen: " + (email == null ? "" : email));

        // ✅ 3) Speichern
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            auth.resetPassword(email, r -> {
                if (!r.ok) {
                    msg.setText(r.message);
                } else {
                    msg.setText("E-Mail zum Zurücksetzen wurde gesendet.");
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
            });
        });
    }
}

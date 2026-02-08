package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager auth = new AuthManager(this);

        // 1) XML laden
        setContentView(R.layout.activity_login);

        // 2) Views aus XML holen
        EditText email = findViewById(R.id.etEmail);
        EditText pw = findViewById(R.id.etPassword);
        TextView error = findViewById(R.id.tvError);

        // 3) Button-Logik
        findViewById(R.id.btnDoLogin).setOnClickListener(v -> {
            auth.login(
                    email.getText().toString().trim(),
                    pw.getText().toString(),
                    r -> {
                        if (!r.ok) {
                            error.setText(r.message);
                        } else {
                            startActivity(new Intent(this, MainMenuActivity.class));
                            finish();
                        }
                    }
            );
        });

        findViewById(R.id.btnReset).setOnClickListener(v ->
                startActivity(new Intent(this, ResetRequestActivity.class)));
    }
}
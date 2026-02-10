package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.sudoku.R;

public class ResetPasswordActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String email = getIntent().getStringExtra("email");

        setContentView(R.layout.activity_reset_password);

        TextView title = findViewById(R.id.tvTitle);
        TextView msg = findViewById(R.id.tvMsg);

        String safeEmail = email == null ? "" : email;
        title.setText(getString(R.string.reset_password_title, safeEmail));

        msg.setText(R.string.reset_only_via_email_link);

        // Direktes Zurücksetzen in der App ist absichtlich deaktiviert:
        // Das Passwort darf nur über den E-Mail-Link zurückgesetzt werden.
        findViewById(R.id.btnSave).setVisibility(View.GONE);

        findViewById(R.id.tvTitle).postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, 1600);
    }
}
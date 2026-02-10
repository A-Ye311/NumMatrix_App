package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;

public class ResetRequestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_request);

        EditText email = findViewById(R.id.etEmail);
        TextView msg = findViewById(R.id.tvMsg);
        View sendButton = findViewById(R.id.btnSend);
        AuthManager authManager = new AuthManager(this);

        sendButton.setOnClickListener(v -> {
            String normalizedEmail = email.getText().toString().trim();
            sendButton.setEnabled(false);
            msg.setText("");

            authManager.resetPassword(normalizedEmail, result -> runOnUiThread(() -> {
                sendButton.setEnabled(true);
                if (!result.ok) {
                    msg.setText(result.message);
                    return;
                }

                Toast.makeText(this, R.string.reset_email_sent, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }));
        });
    }
}
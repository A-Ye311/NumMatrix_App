package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sudoku.R;

public class ResetRequestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reset_request);

        EditText email = findViewById(R.id.etEmail);
        TextView msg = findViewById(R.id.tvMsg);

        findViewById(R.id.btnSend).setOnClickListener(v -> {
            msg.setText(R.string.reset_email_sent);
            Intent i = new Intent(this, ResetPasswordActivity.class);
            i.putExtra("email", email.getText().toString().trim());
            startActivity(i);
        });
    }
}

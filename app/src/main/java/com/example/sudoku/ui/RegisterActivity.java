package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;

/** Registrierungsseite für neue Benutzer. */
public class RegisterActivity extends Activity {
    /** Baut die Ansicht der Seite auf. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager auth = new AuthManager(this);


        setContentView(R.layout.activity_register);

        EditText email = findViewById(R.id.etEmail);
        EditText pw = findViewById(R.id.etPassword);
        EditText pw2 = findViewById(R.id.etPassword2);
        TextView error = findViewById(R.id.tvError);


        findViewById(R.id.btnDoRegister).setOnClickListener(v ->
                auth.register(
                        email.getText().toString().trim(),
                        pw.getText().toString(),
                        pw2.getText().toString(),
                        r -> {
                            if (!r.ok) {
                                error.setText(r.message);
                                return;
                            }

                            if (r.message != null && !r.message.isEmpty()) {
                                error.setText(r.message);
                                return;
                            }

                            startActivity(new Intent(this, MainMenuActivity.class));
                            finish();
                        }
                ));
    }
}

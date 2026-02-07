package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.sudoku.R;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.sudoku.auth.AuthManager;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // XML laden
        setContentView(R.layout.activity_start);

        // Buttons aus XML
        TextView loggedInInfo = findViewById(R.id.tvLoggedInInfo);
        Button btnContinue = findViewById(R.id.btnContinue);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        updateLoginState(loggedInInfo, btnContinue);
    }

        @Override
        protected void onResume() {
            super.onResume();
            updateLoginState(findViewById(R.id.tvLoggedInInfo),
                    findViewById(R.id.btnContinue));
        }
        private void updateLoginState(TextView loggedInInfo, Button btnContinue) {
            AuthManager auth = new AuthManager(this);
            String email = auth.currentUserEmail();
            if (email != null) {
                loggedInInfo.setText("Eingeloggt als: " + email);
                loggedInInfo.setVisibility(View.VISIBLE);
                btnContinue.setVisibility(View.VISIBLE);
                btnContinue.setOnClickListener(v ->
                        startActivity(new Intent(this, MainMenuActivity.class)));
            } else {
                loggedInInfo.setVisibility(View.GONE);
                btnContinue.setVisibility(View.GONE);
            }
    }
}

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

        AuthManager auth = new AuthManager(this);
        // XML laden
        setContentView(R.layout.activity_start);

        // Buttons aus XML
        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        TextView loggedInInfo = findViewById(R.id.tvLoggedInInfo);
        Button continueButton = findViewById(R.id.btnContinue);
        String email = auth.currentUserEmail();
        if (email != null) {
            loggedInInfo.setText("Eingeloggt als: " + email);
            loggedInInfo.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);
            continueButton.setOnClickListener(v ->
                    startActivity(new Intent(this, MainMenuActivity.class)));
        } else {
            loggedInInfo.setVisibility(View.GONE);
            continueButton.setVisibility(View.GONE);
        }
    }
}

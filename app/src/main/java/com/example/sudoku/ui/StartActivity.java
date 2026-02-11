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
    private TextView loggedInInfo;
    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // XML laden
        setContentView(R.layout.activity_start);

        loggedInInfo = findViewById(R.id.tvLoggedInInfo);
        btnContinue = findViewById(R.id.btnContinue);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        updateLoginState();
    }
    //kann von Login oder Register zurückkommen
    @Override
    protected void onResume() {
        super.onResume();
        updateLoginState();
    }
    //Holt den aktuellen Login-Status aus Firebase
    private void updateLoginState() {
        AuthManager auth = new AuthManager(this);
        String email = auth.currentUserEmail();
        //Bereits eingeloggte Nutzer müssen sich nicht erneut anmelden
        if (email != null) {
            loggedInInfo.setText(getString(R.string.logged_in_as, email));
            loggedInInfo.setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.VISIBLE);
            btnContinue.setOnClickListener(v ->
                    startActivity(new Intent(this, MainMenuActivity.class)));
            return;
        }
        //Nicht eingeloggte Nutzer
        loggedInInfo.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);
        btnContinue.setOnClickListener(null);
    }
}

package com.example.sudoku.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sudoku.auth.AuthManager;
import com.example.sudoku.stats.StatsManager;

public class StatisticsActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager auth = new AuthManager(this);
        StatsManager stats = new StatsManager(this);

        String email = auth.currentUserEmail();
        TextView tv = new TextView(this);
        tv.setText(email == null ? "Nicht eingeloggt." : stats.getSummary(email));

        setContentView(tv);
    }
}

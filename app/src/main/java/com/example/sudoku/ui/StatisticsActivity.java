package com.example.sudoku.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sudoku.auth.AuthManager;
import com.example.sudoku.stats.StatsManager;
import com.example.sudoku.R;

public class StatisticsActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_statistics);

        AuthManager auth = new AuthManager(this);
        StatsManager stats = new StatsManager();

        String email = auth.currentUserEmail();
        TextView tv = findViewById(R.id.tvStats);
        if (email == null) {
            tv.setText("Nicht eingeloggt.");
        } else {
            tv.setText("Lade Statistik...");
            stats.getSummary(email, tv::setText);
        }
    }
}

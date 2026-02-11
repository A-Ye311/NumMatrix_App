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
        StatsManager stats = new StatsManager(); // Daten aus Firestore

        String uid = auth.currentUserUid();
        TextView tv = findViewById(R.id.tvStats);

        if (uid == null) {
            tv.setText(R.string.not_logged_in); //Ohne Login keine Daten
        } else {
            tv.setText(R.string.loading_stats); // Mit Login werden Daten angezeigt
            stats.getSummary(uid, tv::setText);
        }
    }
}
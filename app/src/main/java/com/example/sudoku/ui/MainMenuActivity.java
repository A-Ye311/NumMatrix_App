package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainMenuActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        Button start = new Button(this);
        start.setText("SPIEL STARTEN");
        start.setOnClickListener(v -> startActivity(new Intent(this, DifficultyActivity.class)));

        Button stats = new Button(this);
        stats.setText("STATISTIK");
        stats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));

        Button settings = new Button(this);
        settings.setText("EINSTELLUNG");
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        root.addView(start);
        root.addView(stats);
        root.addView(settings);
        setContentView(root);
    }
}

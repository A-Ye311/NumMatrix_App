package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.sudoku.R;

/** Hauptmenü der App mit Navigation zu den wichtigsten Bereichen. */
public class MainMenuActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_menu); //Hauptmenü

        Button start = findViewById(R.id.btnStartGame);
        Button stats = findViewById(R.id.btnStats);
        Button settings = findViewById(R.id.btnSettings);
        start.setOnClickListener(v -> startActivity(new Intent(this, DifficultyActivity.class)));
        stats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }
}

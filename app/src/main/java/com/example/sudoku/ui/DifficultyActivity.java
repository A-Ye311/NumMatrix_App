package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.sudoku.R;

public class DifficultyActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_difficulty);
        Button easy = findViewById(R.id.btnEasy);
        Button medium = findViewById(R.id.btnMedium);
        Button hard = findViewById(R.id.btnHard);
        easy.setOnClickListener(v -> startGame("EINFACH"));
        medium.setOnClickListener(v -> startGame("MITTEL"));
        hard.setOnClickListener(v -> startGame("SCHWER"));
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
        finish();
    }
}

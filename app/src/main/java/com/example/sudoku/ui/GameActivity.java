package com.example.sudoku.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;
import com.example.sudoku.game.SudokuGame;
import com.example.sudoku.game.SudokuGenerator;
import com.example.sudoku.stats.StatsManager;

public class GameActivity extends Activity {

    private SudokuGame game;
    private final EditText[][] cells = new EditText[9][9];
    private TextView mistakesView;

    private String difficulty;
    private StatsManager stats;
    private AuthManager auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "EINFACH";

        auth = new AuthManager(this);
        stats = new StatsManager(this);

        SudokuGenerator gen = new SudokuGenerator();
        game = new SudokuGame(gen.generate(difficulty));

        // ✅ 1) Erst Layout laden!
        setContentView(R.layout.activity_game);

        // ✅ 2) Views aus XML holen
        TextView diffView = findViewById(R.id.tvDifficulty);
        mistakesView = findViewById(R.id.tvMistakes);
        GridLayout grid = findViewById(R.id.sudokuGrid);

        diffView.setText("Schwierigkeit: " + difficulty);
        updateMistakes();

        // ✅ 3) Zellen dynamisch ins GridLayout einfügen
        int size = (int) (getResources().getDisplayMetrics().widthPixels / 9.5);

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {

                final EditText cell = new EditText(this);
                cell.setBackgroundResource(R.drawable.cell_border);
                cell.setTextSize(18);
                cell.setGravity(Gravity.CENTER);
                cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                cell.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(1) });

        // ✅ feste Größe + Position im Grid
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(r),
                        GridLayout.spec(c)
                );
                params.width = size;
                params.height = size;

        // ✅ Abstände (und 3x3 Block-Trennung)
                params.setMargins(1, 1, 1, 1);
                if (r % 3 == 0) params.topMargin = 6;
                if (c % 3 == 0) params.leftMargin = 6;

                cell.setLayoutParams(params);


                int v = game.getCell(r, c);
                if (v != 0) {
                    cell.setText(String.valueOf(v));
                    cell.setEnabled(false);
                } else {
                    final int rr = r, cc = c;
                    cell.addTextChangedListener(new TextWatcher() {
                        boolean inner;

                        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (inner) return;
                            inner = true;

                            String t = s.toString().trim();
                            if (t.isEmpty()) { inner = false; return; }

                            int val;
                            try { val = Integer.parseInt(t); }
                            catch (Exception e) { cell.setText(""); inner = false; return; }

                            int res = game.trySet(rr, cc, val);

                            if (res == 1) {
                                Toast.makeText(GameActivity.this, "Fehler!", Toast.LENGTH_SHORT).show();
                                cell.setText("");
                                updateMistakes();
                            } else if (res == 2) {
                                updateMistakes();
                                onGameEnd(false);
                            } else {
                                cell.setEnabled(false);
                                if (game.isComplete()) onGameEnd(true);
                            }

                            inner = false;
                        }
                    });
                }

                cells[r][c] = cell;
                grid.addView(cell);
            }
        }
    }

    private void updateMistakes() {
        if (mistakesView != null) {
            mistakesView.setText("Fehler: " + game.getMistakes() + " / 3");
        }
    }

    private void onGameEnd(boolean win) {
        String email = auth.currentUserEmail();
        if (email != null) stats.recordGame(email, difficulty, win);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(win ? "Spiel gewonnen" : "Spiel vorbei");
        b.setMessage(win ? "Glückwunsch!" : "3 Fehler erreicht.");

        b.setPositiveButton("NOCHMAL", (d, which) -> {
            startActivity(new Intent(this, DifficultyActivity.class));
            finish();
        });

        b.setNegativeButton("BEENDEN", (d, which) -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        });

        b.setCancelable(false);
        b.show();
    }
}

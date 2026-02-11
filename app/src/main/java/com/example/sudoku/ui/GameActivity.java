package com.example.sudoku.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private static final int GRID = 9;

    private SudokuGame game;
    private final EditText[][] cells = new EditText[GRID][GRID];

    private TextView mistakesView;
    private TextView timerView;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMs;
    private boolean timerRunning;

    private String difficulty;
    private StatsManager stats;
    private AuthManager auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "EINFACH";

        //Manager erstellen
        auth = new AuthManager(this);
        stats = new StatsManager();
        //Spiel erstellen
        game = new SudokuGame(new SudokuGenerator().generate(difficulty));
        //Views finden
        TextView diffView = findViewById(R.id.tvDifficulty);
        mistakesView = findViewById(R.id.tvMistakes);
        timerView = findViewById(R.id.tvTimer);
        GridLayout grid = findViewById(R.id.sudokuGrid);
        //UI
        diffView.setText(getString(R.string.difficulty_label, difficulty));
        updateMistakes();

        buildGrid(grid);
        grid.post(() -> applyGridSizing(grid));

        startTimer();
    }
    //9x9 Zellen erzeugen
    private void buildGrid(GridLayout grid) {
        grid.removeAllViews();

        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                EditText cell = createCell(r, c);
                // vorgegebene Felder sperren
                int v = game.getCell(r, c);
                if (v != 0) {
                    cell.setText(String.valueOf(v));
                    cell.setEnabled(false);
                } else {
                    attachInputWatcher(cell, r, c);
                }

                cells[r][c] = cell;
                grid.addView(cell);
            }
        }
    }
    //Konfiguration jeder Zelle
    private EditText createCell(int r, int c) {
        EditText cell = new EditText(this);
        cell.setBackgroundResource(R.drawable.cell_border);
        cell.setTextSize(18);
        cell.setGravity(Gravity.CENTER);
        cell.setInputType(InputType.TYPE_CLASS_NUMBER);
        cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c));
        cell.setLayoutParams(params);

        return cell;
    }
    // prüft jede Eingabe in einer Sudoku-Zelle sofort
    private void attachInputWatcher(EditText cell, int r, int c) {
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

                int val = parseDigitOrClear(cell, t);
                if (val == 0) { inner = false; return; }
                //übergibt Zahl an Spiellogik
                int res = game.trySet(r, c, val);
                handleMoveResult(cell, res);

                inner = false;
            }
        });
    }

    private int parseDigitOrClear(EditText cell, String t) {
        try {
            int v = Integer.parseInt(t);
            if (v < 1 || v > 9) throw new NumberFormatException();
            return v;
        } catch (Exception e) {
            cell.setText("");
            return 0;
        }
    }

    private void handleMoveResult(EditText cell, int res) {
        if (res == 1) {
            Toast.makeText(this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            cell.setText("");
            updateMistakes();
            return;
        }

        if (res == 2) {
            updateMistakes();
            onGameEnd(false);
            return;
        }

        // res == 0 => korrekt gesetzt (oder nicht gesetzt, aber hier kommen wir nur mit val 1..9 rein)
        cell.setEnabled(false);
        if (game.isComplete()) onGameEnd(true);
    }

    private void updateMistakes() {
        mistakesView.setText(getString(R.string.mistakes_label, game.getMistakes()));
    }

    private void startTimer() {
        startTimeMs = System.currentTimeMillis();
        timerRunning = true;
        timerHandler.post(timerTick);
    }

    private void stopTimer() {
        timerRunning = false;
        timerHandler.removeCallbacks(timerTick);
    }

    private int elapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startTimeMs) / 1000);
    }

    private void updateTimerText() {
        int total = elapsedSeconds();
        int m = total / 60;
        int s = total % 60;
        timerView.setText(getString(R.string.time_label, m, s));
    }

    private final Runnable timerTick = new Runnable() {
        @Override public void run() {
            if (!timerRunning) return;
            updateTimerText();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void applyGridSizing(GridLayout grid) {
        int thin = dpToPx(1);
        int thick = dpToPx(4);

        int availableWidth = grid.getWidth() - grid.getPaddingLeft() - grid.getPaddingRight();
        int totalMargins = (4 * thick) + (14 * thin);
        int cellSize = (availableWidth - totalMargins) / 9;

        for (int r = 0; r < GRID; r++) {
            for (int c = 0; c < GRID; c++) {
                EditText cell = cells[r][c];
                GridLayout.LayoutParams p = (GridLayout.LayoutParams) cell.getLayoutParams();

                p.width = cellSize;
                p.height = cellSize;

                int left = (c % 3 == 0) ? thick : thin;
                int top = (r % 3 == 0) ? thick : thin;
                int right = (c == 8) ? thick : thin;
                int bottom = (r == 8) ? thick : thin;

                p.setMargins(left, top, right, bottom);
                cell.setLayoutParams(p);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    // Spielende
    private void onGameEnd(boolean win) {
        stopTimer();
        //Stats speichern
        String uid = auth.currentUserUid();
        if (uid != null) stats.recordGame(uid, difficulty, win, elapsedSeconds());

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(win ? R.string.dialog_title_win : R.string.dialog_title_lose);
        b.setMessage(win ? R.string.dialog_msg_win : R.string.dialog_msg_lose);

        b.setPositiveButton(R.string.dialog_positive, (d, which) -> {
            startActivity(new Intent(this, DifficultyActivity.class));
            finish();
        });

        b.setNegativeButton(R.string.dialog_negative, (d, which) -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish(); // schliesst das Game
        });

        b.setCancelable(false);
        b.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
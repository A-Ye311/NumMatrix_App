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

    private SudokuGame game;
    private final EditText[][] cells = new EditText[9][9];
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

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) difficulty = "EINFACH";

        auth = new AuthManager(this);
        stats = new StatsManager();

        SudokuGenerator gen = new SudokuGenerator();
        game = new SudokuGame(gen.generate(difficulty));

        // ✅ 1) Erst Layout laden!
        setContentView(R.layout.activity_game);

        // ✅ 2) Views aus XML holen
        TextView diffView = findViewById(R.id.tvDifficulty);
        mistakesView = findViewById(R.id.tvMistakes);
        timerView = findViewById(R.id.tvTimer);
        GridLayout grid = findViewById(R.id.sudokuGrid);

        diffView.setText(getString(R.string.difficulty_label, difficulty));
        updateMistakes();
        startTimer();

        // ✅ 3) Zellen dynamisch ins GridLayout einfügen
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
                                Toast.makeText(GameActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
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
        grid.post(() -> applyGridSizing(grid));
    }

    private void updateMistakes() {
        if (mistakesView != null) {
            mistakesView.setText(getString(R.string.mistakes_label, game.getMistakes()));        }
    }

    private void applyGridSizing(GridLayout grid) {
        int thin = dpToPx(1);
        int thick = dpToPx(4);

        int availableWidth = grid.getWidth() - grid.getPaddingLeft() - grid.getPaddingRight();
        int totalHorizontalMargins = (4 * thick) + (14 * thin);
        int cellSize = (availableWidth - totalHorizontalMargins) / 9;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                EditText cell = cells[r][c];
                GridLayout.LayoutParams params = (GridLayout.LayoutParams) cell.getLayoutParams();
                params.width = cellSize;
                params.height = cellSize;

                int left = (c % 3 == 0) ? thick : thin;
                int top = (r % 3 == 0) ? thick : thin;
                int right = thin;
                int bottom = thin;
                if (c == 8) right = thick;
                if (r == 8) bottom = thick;
                params.setMargins(left, top, right, bottom);
                cell.setLayoutParams(params);
            }
        }
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
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        return (int) (elapsedMs / 1000);
    }

    private void updateTimerText() {
        if (timerView == null) return;
        int totalSeconds = elapsedSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerView.setText(getString(R.string.time_label, minutes, seconds));
    }

    private final Runnable timerTick = new Runnable() {
        @Override
        public void run() {
            if (!timerRunning) return;
            updateTimerText();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void onGameEnd(boolean win) {
        stopTimer();
        String uid = auth.currentUserUid();
        if (uid != null) stats.recordGame(uid, win, elapsedSeconds());

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(win ? R.string.dialog_title_win : R.string.dialog_title_lose);
        b.setMessage(win ? R.string.dialog_msg_win : R.string.dialog_msg_lose);

        b.setPositiveButton(R.string.dialog_positive, (d, which) -> {
            startActivity(new Intent(this, DifficultyActivity.class));
            finish();
        });

        b.setNegativeButton(R.string.dialog_negative, (d, which) -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
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

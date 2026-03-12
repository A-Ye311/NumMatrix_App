package com.example.sudoku.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sudoku.R;
import com.example.sudoku.auth.AuthManager;
import com.example.sudoku.stats.StatsManager;

import java.util.List;

public class StatisticsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_statistics);

        AuthManager auth = new AuthManager(this);
        StatsManager stats = new StatsManager();

        String uid = auth.currentUserUid();
        TextView stateText = findViewById(R.id.tvStatsState);
        LinearLayout statsRowsContainer = findViewById(R.id.layoutStatsRows);

        if (uid == null) {
            stateText.setText(R.string.not_logged_in);
            return;
        }

        stateText.setText(R.string.loading_stats);
        stats.getSummary(uid,
                rows -> showRows(rows, stateText, statsRowsContainer),
                () -> stateText.setText(R.string.stats_load_error));
    }

    private void showRows(List<StatsManager.StatRow> rows, TextView stateText, LinearLayout statsRowsContainer) {
        clearBodyRows(statsRowsContainer);

        if (rows.isEmpty() || rows.get(0).played == 0) {
            stateText.setText(R.string.stats_no_data);
            return;
        }

        stateText.setText("");
        for (StatsManager.StatRow row : rows) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.addView(createCell(row.label, false));
            rowLayout.addView(createCell(String.valueOf(row.played), true));
            rowLayout.addView(createCell(String.valueOf(row.wins), true));
            rowLayout.addView(createCell(row.avgTime, true));
            rowLayout.addView(createCell(row.bestTime, true));
            statsRowsContainer.addView(rowLayout);
        }
    }

    private TextView createCell(String text, boolean alignEnd) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(8, 8, 8, 8);
        if (alignEnd) {
            cell.setGravity(Gravity.END);
        }
        cell.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        return cell;
    }

    private void clearBodyRows(LinearLayout statsRowsContainer) {
        statsRowsContainer.removeAllViews();
    }
}

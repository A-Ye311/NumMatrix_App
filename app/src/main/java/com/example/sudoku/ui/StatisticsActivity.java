package com.example.sudoku.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
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
        TableLayout statsTable = findViewById(R.id.tableStats);

        if (uid == null) {
            stateText.setText(R.string.not_logged_in);
            return;
        }

        stateText.setText(R.string.loading_stats);
        stats.getSummary(uid,
                rows -> showRows(rows, stateText, statsTable),
                () -> stateText.setText(R.string.stats_load_error));
    }

    private void showRows(List<StatsManager.StatRow> rows, TextView stateText, TableLayout statsTable) {
        clearBodyRows(statsTable);

        if (rows.isEmpty() || rows.get(0).played == 0) {
            stateText.setText(R.string.stats_no_data);
            return;
        }

        stateText.setText("");
        for (StatsManager.StatRow row : rows) {
            TableRow tableRow = new TableRow(this);
            tableRow.addView(createCell(row.label, false));
            tableRow.addView(createCell(String.valueOf(row.played), true));
            tableRow.addView(createCell(String.valueOf(row.wins), true));
            tableRow.addView(createCell(row.avgTime, true));
            tableRow.addView(createCell(row.bestTime, true));
            statsTable.addView(tableRow);
        }
    }

    private TextView createCell(String text, boolean alignEnd) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(8, 8, 8, 8);
        if (alignEnd) {
            cell.setGravity(Gravity.END);
        }
        cell.setLayoutParams(new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));
        return cell;
    }

    private void clearBodyRows(TableLayout statsTable) {
        while (statsTable.getChildCount() > 1) {
            statsTable.removeViewAt(1);
        }
    }
}
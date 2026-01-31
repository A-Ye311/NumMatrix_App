package com.example.sudoku.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

public class DifficultyActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        root.addView(makeBtn("EINFACH"));
        root.addView(makeBtn("MITTEL"));
        root.addView(makeBtn("SCHWER"));

        setContentView(root);
    }

    private Button makeBtn(String diff) {
        Button b = new Button(this);
        b.setText(diff);
        b.setOnClickListener(v -> {
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra("difficulty", diff);
            startActivity(i);
            finish();
        });
        return b;
    }
}

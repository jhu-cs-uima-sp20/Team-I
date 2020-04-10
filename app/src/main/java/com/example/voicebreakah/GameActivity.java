package com.example.voicebreakah;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GameActivity extends AppCompatActivity {

    private BreakoutEngine breakoutEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Initialize gameView and set it as the view
        breakoutEngine = new BreakoutEngine(this, size.x, size.y);
        setContentView(breakoutEngine);
    }

    // This method executes when the player starts the game
    @Override
    protected void onResume() {
        super.onResume();
        breakoutEngine.resume();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        super.onPause();
        breakoutEngine.pause();
    }
}

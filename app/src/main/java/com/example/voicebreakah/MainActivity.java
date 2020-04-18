package com.example.voicebreakah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences myPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPrefs = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(intent);
            }
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        Button skinsButton = findViewById(R.id.skins_button);
        skinsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Skins.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        TextView highscore = (TextView) findViewById(R.id.tv_highscore_val);
        int score = myPrefs.getInt("highscore", 0);
        highscore.setText(Integer.toString(score));
    }

}

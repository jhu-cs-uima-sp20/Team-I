package com.example.voicebreakah;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.os.Bundle;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    private SharedPreferences myPrefs;
    private SharedPreferences.Editor editor;
    Switch musicSwitch;
    Switch soundSwitch;
    Switch voiceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        myPrefs = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        musicSwitch = findViewById(R.id.switch3);
        soundSwitch = findViewById(R.id.switch2);
        voiceSwitch = findViewById(R.id.switch1);

        if (myPrefs.getBoolean("MUSIC_ON_OFF", true)) {
            musicSwitch.setChecked(true);
        } else {
            musicSwitch.setChecked(false);
        }

        if (myPrefs.getBoolean("SOUND_ON_OFF", true)) {
            soundSwitch.setChecked(true);
        } else {
            soundSwitch.setChecked(false);
        }

        if (myPrefs.getBoolean("VOICE_ON_OFF", true)) {
            voiceSwitch.setChecked(true);
        } else {
            voiceSwitch.setChecked(false);
        }

        ImageView backArrow = findViewById(R.id.settings_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        musicSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = myPrefs.edit();
                if (myPrefs.getBoolean("MUSIC_ON_OFF", false)) {
                    editor.putBoolean("MUSIC_ON_OFF", false);
                } else {
                    editor.putBoolean("MUSIC_ON_OFF", true);
                }
                editor.commit();
            }
        });

        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = myPrefs.edit();
                if (myPrefs.getBoolean("SOUND_ON_OFF", true)) {
                    editor.putBoolean("SOUND_ON_OFF", false);
                } else {
                    editor.putBoolean("SOUND_ON_OFF", true);
                }
                editor.commit();
            }
        });

        voiceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = myPrefs.edit();
                if (myPrefs.getBoolean("VOICE_ON_OFF", true)) {
                    editor.putBoolean("VOICE_ON_OFF", false);
                } else {
                    editor.putBoolean("VOICE_ON_OFF", true);
                }
                editor.commit();
            }
        });
    }
}

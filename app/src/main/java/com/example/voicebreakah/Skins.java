package com.example.voicebreakah;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Skins extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageView backButton = (ImageView)findViewById(R.id.skins_back_arrow);
        backButton.setOnClickListener(backListener);
    }

    private View.OnClickListener backListener = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };
}

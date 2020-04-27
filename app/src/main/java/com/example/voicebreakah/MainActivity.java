package com.example.voicebreakah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences myPrefs;
    private SharedPreferences.Editor peditor;
    private boolean newUser;
    private String[] paddleIDs;
    private int paddleIndex;
    private Resources res;
    private ImageView paddleView;
    private int coins;

    Intent music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get rid of status bar
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // get shared preferences
        myPrefs = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        // all the buttons
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


        // paddles
        peditor = myPrefs.edit();
        newUser = myPrefs.getBoolean("newUser",true);
        if(newUser){
            Set<String> paddleIDs = new HashSet();
            paddleIDs.add("00");
            peditor.putInt("coinBalance",0);
            peditor.putStringSet("paddleSkinSet",paddleIDs);
            peditor.putBoolean("newUser",false);
            paddleIndex = 0;

        }
        else{
            paddleIndex = myPrefs.getInt("currPaddleIndex",0);
        }
        peditor.commit();

        // changing paddle
        res = getResources();
        paddleView = findViewById(R.id.paddleView);

        ImageView rightButton = findViewById(R.id.rightButtonView);
        ImageView leftButton = findViewById(R.id.leftButtonView);
        rightButton.setOnClickListener(rightButtonListener);
        leftButton.setOnClickListener(leftButtonListener);


        // background music
        music = new Intent();
        music.setClass(this, BackgroundSoundService.class);
        startService(music);
    }

    /*
    @Override
    protected void onPause() {
        stopService(music);
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startService(music);
    }
    */

    @Override
    protected  void onDestroy() {
        stopService(music);
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();

        TextView highscore = (TextView) findViewById(R.id.tv_highscore_val);
        int score = myPrefs.getInt("highscore", 0);
        highscore.setText(Integer.toString(score));

        TextView coinText = (TextView) findViewById(R.id.coinBalance_val);
        int coinBalance = myPrefs.getInt("coinBalance", 0);
        coinText.setText(Integer.toString(coinBalance));


        Set<String> paddleIDSet = myPrefs.getStringSet("paddleSkinSet",null);
        paddleIDs = paddleIDSet.toArray(new String[paddleIDSet.size()]);

        paddleIndex = myPrefs.getInt("currPaddleIndex",0);
        Drawable d = res.getDrawable(getResources().getIdentifier("paddle_"+paddleIDs[paddleIndex], "drawable", "com.example.voicebreakah"));
        paddleView.setImageDrawable(d);
    }


    private View.OnClickListener rightButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            paddleIndex--;
            if(paddleIndex<0){
                paddleIndex = paddleIDs.length-1;
            }

            Drawable d = res.getDrawable(getResources().getIdentifier("paddle_"+paddleIDs[paddleIndex], "drawable", "com.example.voicebreakah"));
            paddleView.setImageDrawable(d);
            peditor.putInt("currPaddleIndex",paddleIndex);
            peditor.commit();
        }
    };


    private View.OnClickListener leftButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            paddleIndex++;
            if(paddleIndex>=paddleIDs.length){
                paddleIndex = 0;
            }

            Drawable d = res.getDrawable(getResources().getIdentifier("paddle_"+paddleIDs[paddleIndex], "drawable", "com.example.voicebreakah"));
            paddleView.setImageDrawable(d);
            peditor.putInt("currPaddleIndex",paddleIndex);
            peditor.commit();
        }
    };

}

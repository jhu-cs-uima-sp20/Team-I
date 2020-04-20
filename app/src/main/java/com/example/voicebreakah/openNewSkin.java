package com.example.voicebreakah;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.Set;


public class openNewSkin extends AppCompatActivity {

    private Context context;
    private AnimationDrawable treasureAnimation;
    private SharedPreferences myPrefs;
    private SharedPreferences.Editor peditor;
    private Resources res;

    private String[] allPaddles = {"00","01","02","03","04","05","06","07","08","09"};
    private String[] myPaddles;
    int newPaddleIndex;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_open_new_skin);

        res = getResources();
        context = getApplicationContext();
        myPrefs = getApplicationContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        peditor = myPrefs.edit();
        Set<String> myPaddlesSet = myPrefs.getStringSet("paddleSkinSet",null);
        myPaddles = myPaddlesSet.toArray(new String[myPaddlesSet.size()]);
        Random rand = new Random();
        int randomIndex;
        boolean repeat;

        do {
            repeat = false;
            randomIndex = (int) (Math.random() * allPaddles.length);
            for(int i = 0; i< myPaddles.length; i++) {
                if (allPaddles[randomIndex].equals(myPaddles[i])){
                    repeat = true;
                    break;
                }
            }
        }while(repeat);

        newPaddleIndex = randomIndex;
        myPaddlesSet.add(allPaddles[newPaddleIndex]);
        peditor.putStringSet("paddleSkinSet",myPaddlesSet);
        peditor.commit();





        ImageView backArrow = findViewById(R.id.new_skin_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView treasureImage = (ImageView) findViewById(R.id.chestView);
        treasureImage.setBackgroundResource(R.drawable.chest_animation);
        treasureAnimation = (AnimationDrawable) treasureImage.getBackground();

        treasureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("V","hi");
                treasureAnimation.start();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        treasureAnimation.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ImageView newSkinView = findViewById(R.id.skinView);
                String newPaddleName = "paddle_" + allPaddles[newPaddleIndex];
                Drawable d = res.getDrawable(getResources().getIdentifier(newPaddleName, "drawable", "com.example.voicebreakah"));
                newSkinView.setImageDrawable(d);
            }
        }, 1500);   //1.5 seconds
    }

}

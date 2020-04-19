package com.example.voicebreakah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Skins extends AppCompatActivity {

    private SharedPreferences myPrefs;
    private SharedPreferences.Editor peditor;
    private Context context;
    private Resources res;
    private Set<String> myPaddleSet;
    //Set<ImageView> paddleViews;
    ImageView[] skinViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_skins);

        myPrefs = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        peditor = myPrefs.edit();
        res = getResources();


        //paddleViews = new HashSet();
        ImageView skin0 = findViewById(R.id.skin0);
        ImageView skin1 = findViewById(R.id.skin1);
        ImageView skin2 = findViewById(R.id.skin2);
        ImageView skin3 = findViewById(R.id.skin3);
        ImageView skin4 = findViewById(R.id.skin4);
        ImageView skin5 = findViewById(R.id.skin5);
        ImageView skin6 = findViewById(R.id.skin6);
        ImageView skin7 = findViewById(R.id.skin7);
        ImageView skin8 = findViewById(R.id.skin8);
        ImageView skin9 = findViewById(R.id.skin9);
        ImageView[] temp = {skin0, skin1, skin2, skin3, skin4, skin5, skin6, skin7, skin8, skin9};
        skinViews = temp;
        /*paddleViews.add(skin0);
        paddleViews.add(skin1);
        paddleViews.add(skin2);
        paddleViews.add(skin3);
        paddleViews.add(skin4);
        paddleViews.add(skin5);
        paddleViews.add(skin6);
        paddleViews.add(skin7);
        paddleViews.add(skin8);
        paddleViews.add(skin9);*/

        myPaddleSet = myPrefs.getStringSet("paddleSkinSet",null);
        Object[] myPaddles = myPaddleSet.toArray(new String[myPaddleSet.size()]);

        for (int i = 0;i < myPaddles.length && i < skinViews.length; i++ ){
            String paddleSkinName = "paddle_" + myPaddles[i];
            Drawable d = res.getDrawable(getResources().getIdentifier(paddleSkinName, "drawable", "com.example.voicebreakah"));
            skinViews[i].setImageDrawable(d);
        }
        /*if(paddleIDs!=null) {
            Iterator<String> skinIterator = paddleIDs.iterator();
            //Iterator<ImageView> viewIterator = paddleViews.iterator();
            while (skinIterator.hasNext() && viewIterator.hasNext()) {
                String paddleSkinName = "paddle_" + skinIterator.next();
                Drawable d = res.getDrawable(getResources().getIdentifier(paddleSkinName, "drawable", "com.example.voicebreakah"));
                viewIterator.next().setImageDrawable(d);
            }
        }*/

        ImageView backArrow = findViewById(R.id.skins_back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}

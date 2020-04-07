package com.example.voicebreakah;

import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Random;

public class Ball {

    private Rect rect; //* TR change to RectF
    private float xVelocity;
    private float yVelocity;
    private float ballWidth = 10;
    private float ballHeight = 10;

    // constructor
    Ball(){

        xVelocity = 200;
        yVelocity = -400;

        //float x = screenX / 2;
        //float y = screenY - screenY * (float) 0.21;
        rect = new Rect(); //* TR change to RectF()
    }

    // Return rectangle
    Rect getRect(){
        return rect;
    } //* TR change to RectF

    // Update ball movement
    void update(long fps){
        rect.left = (int)(rect.left + (xVelocity / fps)); //* TR get rid of casts
        rect.top = (int)(rect.top + (yVelocity / fps));
        rect.right = (int)(rect.left + ballWidth);
        rect.bottom = (int)(rect.top - ballHeight);

    }

    void reverseYVelocity(){
        yVelocity = -yVelocity;
    }

    void reverseXVelocity(){
        xVelocity = - xVelocity;
    }

    void setRandomXVelocity(){
        Random generator = new Random();
        int answer = generator.nextInt(2);

        if(answer == 0){
            reverseXVelocity();
        }
    }

    void clearObstacleY(float y){//* TR get rid of casts
        rect.bottom = (int)y;
        rect.top = (int)(y - ballHeight);
    }

    void clearObstacleX(float x){//* TR get rid of casts
        rect.left = (int)x;
        rect.right = (int)(x + ballWidth);
    }

    void reset(int x, int y){//* TR get rid of casts
        rect.left = x / 2;
        rect.bottom = (int)(y - y * (float) 0.21 - ballHeight);

        rect.top = (int)(rect.bottom+ballHeight);
        rect.right = (int)(x / 2 + ballWidth);

    }
}
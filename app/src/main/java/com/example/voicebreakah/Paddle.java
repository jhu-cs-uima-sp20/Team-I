package com.example.voicebreakah;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class Paddle {

    // RectF is an object that holds four coordinates - just what we need
    private Rect rect; //*

    // How long will our paddle will be
    private float length;

    // X is the far left of the rectangle which forms our paddle
    private float x;

    // This will hold the pixels per second speed that the paddle will move
    private float paddleSpeed;

    // Which ways can the paddle move
    final int STOPPED = 0;
    final int LEFT = 1;
    final int RIGHT = 2;

    // Is the paddle moving and in which direction
    private int paddleMoving = STOPPED;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    Paddle(int screenX, int screenY){
        // 130 pixels wide and 20 pixels high
        length = 130;
        float height = 20;

        // Start paddle in roughly the screen centre
        x = screenX / 2;

        // Y is the top coordinate
        float y = screenY - screenY * (float) 0.2;

        rect = new Rect((int)x, (int)y, (int)(x + length), (int)(y + height)); //* to reverse get rid of (int)

        // How fast is the paddle in pixels per second
        paddleSpeed = 350;
        //Drawable drawable = getResources().getDrawable(R.drawable.my_drawable);
        //drawable.setBounds(rect);
        //drawable.draw(canvas);
    }

    // This is a getter method to make the rectangle that
    // defines our paddle available in BreakoutView class
    Rect getRect(){
        return rect;
    } //* to reverse make return type RectF

    // This method will be used to change/set if the paddle is going left, right or nowhere
    void setMovementState(int state){
        paddleMoving = state;
    }

    // This update method will be called from update in BreakoutEngine
    // It determines if the paddle needs to move and changes the coordinates
    // contained in rect if necessary
    void update(long fps){
        if(paddleMoving == LEFT){
            x = x - paddleSpeed / fps;
        }

        if(paddleMoving == RIGHT){
            x = x + paddleSpeed / fps;
        }

        rect.left = (int) x; //* to reverse get rid of (int)
        rect.right = (int)(x + length); //* ^
    }
}

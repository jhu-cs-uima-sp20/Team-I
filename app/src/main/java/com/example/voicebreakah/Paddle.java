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


    private float screenX;

    // Which ways can the paddle move
    final int STOPPED = 0;
    final int LEFT = 1;
    final int RIGHT = 2;

    // Is the paddle moving and in which direction
    private int paddleMoving = STOPPED;


    /** constructor method
     * @param screenX screen wdith
     * @param screenY screen height
     */
    Paddle(int screenX, int screenY){
        this.screenX = screenX;

        length = 150;   // 150 pixels wide
        float height = 40;  // and 40 pixels high
        x = screenX / 2;  // Start paddle in roughly the screen centre
        float y = screenY - screenY * (float) 0.2;  // Y is the top coordinate

        rect = new Rect((int)x, (int)y, (int)(x + length), (int)(y + height)); //* to reverse get rid of (int)

        // How fast is the paddle in pixels per second
        paddleSpeed = 1000;

        //Drawable drawable = getResources().getDrawable(R.drawable.my_drawable);
        //drawable.setBounds(rect);
        //drawable.draw(canvas);
    }


    /** This is a getter method to make the rectangle that defines our paddle available
     * in BreakoutView class
     */
    Rect getRect(){
        return rect;
    } //* to reverse make return type RectF


    /** This method will be used to change/set if the paddle is going left, right or nowhere */
    void setMovementState(int state){
        paddleMoving = state;
    }


    /** This update method will be called from update in BreakoutEngine
     * It determines if the paddle needs to move and changes the coordinates
     * contained in rect if necessary
     */
    void update(long fps, int speed){
        paddleSpeed = speed;

        if (paddleMoving == LEFT) {
            if (rect.left > 0) {
                x = x - paddleSpeed / fps;
            }
        } else if (paddleMoving == RIGHT) {
            if (rect.right < screenX) {
                x = x + paddleSpeed / fps;
            }
        }

        rect.left = (int) x; //* to reverse get rid of (int)
        rect.right = (int) (x + length); //* ^
    }
}

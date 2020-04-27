package com.example.voicebreakah;

import android.graphics.Rect;
import android.graphics.RectF;

public class Brick {

    private Rect rect; //* TRhange to RectF

    private boolean isVisible;

    Brick(int row, int column, int width, int height, int skip){
        isVisible = true;
        int padding = 1;
        rect = new Rect((int)(column * width + padding), //* TR get rid of casts and change to RectF
                (int)(row * height + padding + skip),
                (int)(column * width + width - padding),
                (int)(row * height + height - padding + skip));
    }

    Rect getRect(){
        return this.rect;
    } //* TR change to RectF

    void setInvisible(){
        isVisible = false;
    }

    boolean getVisibility(){
        return isVisible;
    }

}

package com.example.voicebreakah;

import android.graphics.Rect;
import android.graphics.RectF;

public class Brick {

    private Rect rect; //* TRhange to RectF

    private boolean isVisible;

    Brick(int row, int column, int width, int height, boolean rb, boolean half, int skip){
        isVisible = true;
        int padding = 1;

        if (rb) {
            int start = column * width + padding - width / 2;
            if (half) {
                if (start < 0)
                    rect = new Rect((int) 0,
                            (int) (row * height + padding + skip),
                            (int) (width / 2 - padding),
                            (int) (row * height + height - padding + skip));
                else
                    rect = new Rect((int) start,
                            (int) (row * height + padding + skip),
                            (int) (column * width + width / 2 - padding - width / 2),
                            (int) (row * height + height - padding + skip));
            } else {
                rect = new Rect((int) start,
                        (int) (row * height + padding + skip),
                        (int) (column * width + width - padding - width / 2),
                        (int) (row * height + height - padding + skip));
            }
        } else {
            rect = new Rect((int) (column * width + padding), //* TR get rid of casts and change to RectF
                    (int) (row * height + padding + skip),
                    (int) (column * width + width - padding),
                    (int) (row * height + height - padding + skip));
        }
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

package com.example.voicebreakah;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

class BreakoutEngine extends SurfaceView implements Runnable{

    // This is our thread
    private Thread gameThread = null;

    // This is new. We need a SurfaceHolder
    // When we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    private SurfaceHolder ourHolder;

    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;

    // Width and height of screen
    private int screenX;
    private int screenY;

    // This variable tracks the game frame rate
    private long fps;

    // This is used to help calculate the fps
    private long timeThisFrame;

    // The player's paddle
    Paddle paddle;

    // A ball
    Ball ball;

    // Up to 200 bricks
    Brick[] bricks = new Brick[200];
    int numBricks = 0;

    // For sound FX
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    // The score
    int score = 0;

    // Lives
    int lives = 3;
    int level = 1;
    boolean newGame = true;

    // The constructor is called when the object is first created
    public BreakoutEngine(Context context, int x, int y) {
        // This calls the default constructor to setup the rest of the object
        super(context);

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        // Initialize screenX and screenY because x and y are local
        screenX = x;
        screenY = y;

        // Initialize the player's paddle
        paddle = new Paddle(screenX, screenY);

        // Create a ball
        ball = new Ball();

        // Load the sounds
        // This SoundPool is deprecated but don't worry
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        restart();
    }

    // Runs when the OS calls onPause on BreakoutActivity method
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // Runs when the OS calls onResume on BreakoutActivity method
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (playing) {


            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            // Update the frame
            if(!paused){
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }
    }

    private void update(){
        // Move the paddle if required
        paddle.update(fps);

        // Update the ball
        ball.update(fps);

        // Check for ball colliding with a brick
        for(int i = 0; i < numBricks; i++){

            if (bricks[i].getVisibility()){

                if(Rect.intersects(bricks[i].getRect(),ball.getRect())) {//*change to RectF.intersects
                    bricks[i].setInvisible();
                    ball.reverseYVelocity();
                    score = score + 10;
                    soundPool.play(explodeID, 1, 1, 0, 0, 1);
                }
            }
        }

        // Check for ball colliding with paddle
        if(Rect.intersects(paddle.getRect(),ball.getRect())) { //*change to RectF.intersects
            //ball.setRandomXVelocity();
            ball.setXVelocity(ball.getXVelocity());
            ball.reverseYVelocity();
            ball.clearObstacleY(paddle.getRect().top - 10);
            soundPool.play(beep1ID, 1, 1, 0, 0, 1);
        }

        // Bounce the ball back when it hits the bottom of screen
        // And deduct a life
        if(ball.getRect().bottom > screenY){
            //ball.reverseYVelocity();
            ball.clearObstacleY(screenY - 2);


            newGame = true;
            // Lose a life
            //lives --;
            soundPool.play(loseLifeID, 1, 1, 0, 0, 1);
            paused = true;
            restart();
            /*if(lives == 0){
                paused = true;
                restart();
                newGame == true;
            }*/

        }

        // Bounce the ball back when it hits the top of screen
        if(ball.getRect().top < 0){
            ball.reverseYVelocity();
            ball.clearObstacleY(12);
            soundPool.play(beep2ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits left wall bounce
        if(ball.getRect().left < 0){
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // If the ball hits right wall bounce
        if(ball.getRect().right > screenX - 20){
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 42);
            soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // Make sure paddle doesn't go off the left or right side of the screen
        if (paddle.getRect().right > screenX - 10) {
            // can't move right anymore
        } else if (paddle.getRect().left < 0) {
            // can't move left anymore
        }


        // Pause if cleared screen
        if(score == numBricks * 10){
            paused = true;
            level++;
            ball.setSpeedFactor(level);
            restart();
        }
    }

    void restart(){
        // Put the ball back to the start

        if (newGame == true) {
            level = 1;
            ball.setSpeedFactor(level);
            newGame = false;
        }
        ball.reset(screenX, screenY);
        paddle = new Paddle(screenX, screenY);
        int brickWidth = screenX / 3;
        int brickHeight = screenY / 30;

        // Build a wall of bricks
        numBricks = 0;

        for(int column = 0; column < 3; column ++ ){
            for(int row = 0; row < 1; row ++ ){
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks ++;
            }
        }

        // Reset scores and lives
        score = 0;
        lives = 3;

    }

    private void draw(){
        // Make sure our drawing surface is valid or game will crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.argb(255,  140, 207, 255));

            // Draw everything to the screen

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  242, 12, 12));
            //Drawable paddle_pic = getResources().getDrawable(R.drawable.paddle_pink);
            //drawable.setBounds(myRect);
            //drawable.draw(canvas);
            Resources res = getResources();
            Drawable d = res.getDrawable(R.drawable.paddle_pink);
            //PictureDrawable paddle_pic = new PictureDrawable(drawable);
            // Draw the paddle
            d.setBounds((paddle.getRect()));
            d.draw(canvas);

            //canvas.drawRect(paddle.getRect(), paint);
            //canvas.drawPicture(drawable,paddle.getRect());

            // Draw the ball
            canvas.drawRect(ball.getRect(), paint);

            // Change the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // Draw the HUD
            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  0, 0, 0));

            // Draw the score
            paint.setTextSize(70);
            //canvas.drawText("Score: " + score + "   Lives: " + lives, 10,80, paint);
            // Show everything we have drawn
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;

                if(motionEvent.getX() > screenX / 2){
                    paddle.setMovementState(paddle.RIGHT);
                }
                else{
                    paddle.setMovementState(paddle.LEFT);
                }

                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                paddle.setMovementState(paddle.STOPPED);
                break;
        }

        return true;
    }
}
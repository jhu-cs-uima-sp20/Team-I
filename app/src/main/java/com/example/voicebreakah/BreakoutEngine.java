package com.example.voicebreakah;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

class BreakoutEngine extends SurfaceView implements Runnable{

    // This is our thread
    private Thread gameThread = null;
    //private Thread gameOverThread = null;

    // This is new. We need a SurfaceHolder when we use Paint and Canvas in a thread
    // We will see it in action in the draw method soon.
    private SurfaceHolder ourHolder;

    // A boolean which we will set and unset when the game is running- or not.
    private volatile boolean playing;

    // Game is paused at the start
    private boolean paused = true;

    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;

    // SharedPreferences for highscore
    private SharedPreferences myPrefs;
    private Context context;

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
    int numBricks; // = 0;
    int bricksLeft; // = 0;

    // For sound FX
    SoundPool soundPool;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    // The score
    int score = 0;
    //int prevScore = 0;

    boolean newGame;
    boolean gameOver;

    // Lives
    int lives = 3;
    int level = 1;
    float speedFactor = 1;


    // Paddle speed
    int speed = 100;

    // player touching screen
    boolean touching = false;



    /** The constructor is called when the object is first created */
    public BreakoutEngine(Context context, int x, int y) {
        // This calls the default constructor to setup the rest of the object
        super(context);
        Log.d("engine", "we're in engine");

        myPrefs = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE);
        this.context = context;

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        // Initialize screenX and screenY because x and y are local
        screenX = x;
        screenY = y;

        // Initialize the player's paddle
        paddle = new Paddle(screenX, screenY);
        ball = new Ball();
        numBricks = 0;
        bricksLeft = 0;

        speedFactor = 1;

        newGame = true;
        gameOver = false;

        // Load the sounds
        // This SoundPool is deprecated but don't worry
        /*
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

        } catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }*/

        restart();
    }


    /** Runs when the OS calls onPause on BreakoutActivity method */
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }


    /** Runs when the OS calls onResume on BreakoutActivity method */
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }


    /** Actual game */
    @Override
    public void run() {
        while (playing) {
            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            if(!paused){
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame; can then use the result to time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

        }
    }


    /** update method to call in-game */
    private void update(){
        // Move the paddle if required
        paddle.update(fps, speed);

        // Update the ball
        ball.update(fps);

        if (touching) {
            speed += 15;
        }

        // Check for ball colliding with a brick
        for(int i = 0; i < numBricks; i++){
            if (bricks[i].getVisibility()){
                if(Rect.intersects(bricks[i].getRect(),ball.getRect())) {  //*change to RectF.intersects
                    bricks[i].setInvisible();
                    ball.reverseYVelocity();
                    score = score + 10;
                    bricksLeft--;
                    //soundPool.play(explodeID, 1, 1, 0, 0, 1);
                }
            }
        }

        // Check for ball colliding with paddle
        if(Rect.intersects(paddle.getRect(),ball.getRect())) { //*change to RectF.intersects
            //ball.setRandomXVelocity();
            ball.setXVelocity(ball.getXVelocity());
            ball.reverseYVelocity();
            ball.clearObstacleY(paddle.getRect().top - 10);
            //soundPool.play(beep1ID, 1, 1, 0, 0, 1);
        }


        // If ball hits bottom of screen, game over
        if(ball.getRect().bottom > screenY){
            //ball.reverseYVelocity();
            ball.clearObstacleY(screenY - 2);
            paused = true;
            gameOver = true;
            // soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

            // update highscore
            int currHS = myPrefs.getInt("highscore", 0);
            if (score > currHS) {
                SharedPreferences.Editor peditor = myPrefs.edit();
                peditor.putInt("highscore", score);
                peditor.commit();
            }

        } else if (ball.getRect().top < 0){
            ball.reverseYVelocity();
            ball.clearObstacleY(12);
            //soundPool.play(beep2ID, 1, 1, 0, 0, 1);

        } else if(ball.getRect().left < 0){
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
            //soundPool.play(beep3ID, 1, 1, 0, 0, 1);

        } else if(ball.getRect().right > screenX){
            ball.reverseXVelocity();
            ball.clearObstacleX(screenX - 42);
            //soundPool.play(beep3ID, 1, 1, 0, 0, 1);
        }

        // Pause if cleared screen
        if (bricksLeft == 0) {
            paused = true;
            //level++;
            speedFactor += 0.3;
            ball.setSpeedFactor(speedFactor);
            restart();
        }
    }


    // can use later, when have new game option instead of returning to main
    private void newGame() {
        gameOver = false;
        score = 0;
        restart();
    }


    /** when starting a new level */
    void restart(){
        Log.d("restart", "restart, " + speedFactor);

        ball.reset(screenX, screenY);
        paddle = new Paddle(screenX, screenY);
        int brickWidth = screenX / 3;
        int brickHeight = screenY / 30;

        // Build a wall of bricks
        numBricks = 0;
        for(int column = 0; column < 3; column ++ ){
            for(int row = 0; row < 1; row ++ ){
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                numBricks++;
                bricksLeft++;
            }
        }
    }


    /** Render our game screen */
    private void draw() {
        // Make sure our drawing surface is valid or game will crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background blue color
            canvas.drawColor(Color.argb(255,  140, 207, 255));

            // Draw everything to the screen

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  242, 12, 12));
            Resources res = getResources();
            Drawable d = res.getDrawable(R.drawable.paddle_pink);

            //drawable.setBounds(myRect);
            //drawable.draw(canvas);
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
            paint.setTextSize(40);
            canvas.drawText("Score: " + score, 10,80, paint);
            //canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 80, paint);

            if (gameOver) {
                Rect go = new Rect(200, 200, screenX - 200, screenY - 400);
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawRect(go, paint);

                paint.setColor(Color.argb(255, 0, 0, 0));
                paint.setTextSize(50);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Your score: " + score, screenX / 2,
                        screenY / 2 - 100, paint);
                paint.setTextAlign(Paint.Align.LEFT);
            }

            // Show everything we have drawn
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    /** The SurfaceView class implements onTouchListener, so we can override this method and
     * detect screen touches
     * @param motionEvent some motion
     * @return true
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Our code here
        switch (motionEvent.getActionMasked()) {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;
                touching = true;
                if (motionEvent.getX() > screenX / (float) 2) {
                    paddle.setMovementState(paddle.RIGHT);
                } else {
                    paddle.setMovementState(paddle.LEFT);
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                touching = false;
                speed = 100;
                paddle.setMovementState(paddle.STOPPED);
                break;
        }

        // if game over, return to main screen
        if (gameOver) {
            if (MotionEvent.ACTION_DOWN == 0) {
                //newGame();
                context.startActivity(new Intent(context,MainActivity.class));
            }
        }

        return true;
    }
}
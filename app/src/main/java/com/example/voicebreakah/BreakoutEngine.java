package com.example.voicebreakah;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import ca.uol.aig.fftpack.RealDoubleFFT;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
    private SharedPreferences.Editor peditor;
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
    Drawable paddleSkin;

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
    boolean pauseMenu;


    boolean recording=false;
    // Lives
    int lives = 3;
    int level = 1;
    float speedFactor = 1;


    // Paddle speed
    int speed = 100;

    // player touching screen
    boolean touching = false;

    private RealDoubleFFT transformer;
    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    int bufferSize = AudioRecord.getMinBufferSize(frequency,
            channelConfiguration, audioEncoding);
    int blockSize = 256;
    AudioRecord audioRecord;
    short[] buffer;
    double[] toTransform;
    double targetLocation;
    double voiceScaleFactor;


    private Rect homeR;
    private Rect playAgainR;
    private Rect pauseBtnR;
    private Rect resumeR;
    private Rect newGameR;

    int bufferTop = 0;




    /** The constructor is called when the object is first created */
    public BreakoutEngine(Context context, int x, int y) {
        // This calls the default constructor to setup the rest of the object
        super(context);
        Log.d("engine", "we're in engine");

        myPrefs = context.getSharedPreferences("PREFS",Context.MODE_PRIVATE);
        this.context = context;
        peditor = myPrefs.edit();

        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();

        // Initialize screenX and screenY because x and y are local
        screenX = x;
        screenY = y;

        // buffer from top of the screen
        // this is if we want a space between top of the screen and the first row of bricks,
        // leaving space for the pause button and score, or we can just have them overlap
        bufferTop = screenX / 12 + screenX / 24;


        targetLocation=screenX/2;
        voiceScaleFactor=screenX/100.0;
        // Initialize the player's paddle
        paddle = new Paddle(screenX, screenY);

        recording=false;




        int paddleIndex = myPrefs.getInt("currPaddleIndex",0);
        Set<String> paddleSet = myPrefs.getStringSet("paddleSkinSet",null);
        String[] myPaddles = paddleSet.toArray(new String[paddleSet.size()]);
        String paddleName = "paddle_"+ myPaddles[paddleIndex];
        Resources res = getResources();
        paddleSkin = res.getDrawable(getResources().getIdentifier(paddleName, "drawable", "com.example.voicebreakah"));
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

    public void initializeAudio(){
        Log.d("init", "initialized");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, bufferSize);
        buffer = new short[blockSize];
        transformer=new RealDoubleFFT(blockSize);
        toTransform = new double[blockSize];
        audioRecord.startRecording();
        recording=true;
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

            // Update the frame+
            if(!paused){
                update();
            }
            draw();  // Draw the frame

            // Calculate the fps this frame; can then use the result to time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }


    /** update method to call in-game */
    private void update(){

        //handle voice controls
        /*
        if(targetLocation>paddle.getRect().centerX()){
            speed += 10;
            paddle.setMovementState(paddle.RIGHT);
        }
        else if(targetLocation<paddle.getRect().centerX()){
            speed += 10;
            paddle.setMovementState(paddle.LEFT);
        }
        else
            paddle.setMovementState(paddle.STOPPED);

        if(recording==true) {
            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / 32768.0; // signed
                // 16
            }
            //Log.d("array", "array: " + Arrays.toString(toTransform));
            transformer.ft(toTransform);

            double max=0;
            double maxIndex=0;
            for (int i = 1; i < blockSize; i++){
                if(toTransform[i]>max){
                    maxIndex=i;
                    max=toTransform[i];
                }
            }
            targetLocation=maxIndex*voiceScaleFactor;
            //Log.d("max", paddle.getRect().centerX()+ " "+targetLocation);
        }*/

        // Move the paddle if required
        paddle.update(fps, speed);

        // Update the ball
        ball.update(fps);

        if (touching) {
            speed += 50;
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
            ball.clearObstacleY(bufferTop);
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
        boolean halfBrick = false;
        int numCols = 3;
        int numRows = 1;
        for(int column = 0; column < numCols; column++ ){
            for(int row = 0; row < numRows; row++ ) {
                if (row % 2 == 1 && )
                bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight, 0);
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
            //Drawable d = res.getDrawable(R.drawable.paddle_pink);
            Drawable d = paddleSkin;

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

            // Pause button
            Bitmap pauseBtn = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause_button);
            float size = (float) screenX / 12;
            pauseBtn = scaleDown(pauseBtn, size, true);
            canvas.drawBitmap(pauseBtn, size / 4, size / 4, paint);
            pauseBtnR = new Rect((int) size / 4, (int) size / 4,
                    (int) (size / 4 + pauseBtn.getWidth()), (int) (size / 4 + pauseBtn.getHeight()));


            // Score
            paint.setColor(Color.argb(255,  0, 0, 0));
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(40);
            canvas.drawText(Integer.toString(score), screenX - size / 4,size / 4 + 40, paint);
            paint.setTextAlign(Paint.Align.LEFT);


            // Game over screen
            if (gameOver) {
                drawGameOver();
            }
            if (pauseMenu) {
                drawPauseMenu();
            }

            // Show everything we have drawn
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }


    /** Draws the pause menu */
    private void drawPauseMenu() {
        // draw background
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.pause_menu_background);
        bitmap = scaleDown(bitmap, (float) screenX - 120, true);
        double h = bitmap.getHeight();
        double w = bitmap.getWidth();
        int x = screenX / 2 - (int) w / 2;
        int y = screenY / 2 - (int) (h * 0.5);
        paint = new Paint();
        canvas.drawBitmap(bitmap, x, y, paint);

        // get all the bitmaps
        Bitmap pauseTitle = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.pause_title);
        Bitmap resume = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.resume_btn);
        Bitmap home = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_home);
        Bitmap newGame = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.new_game_btn);


        pauseTitle = scaleDown(pauseTitle, (float) (w - 250), true);
        canvas.drawBitmap(pauseTitle, screenX / 2 - pauseTitle.getWidth()/2, y + 60, paint);

        resume = scaleDown(resume, (float) (w / 2.5), true);
        int a = screenX / 2 - resume.getWidth()/2;
        int b = (int) (screenY / 2 - y * 0.4);
        canvas.drawBitmap(resume, a, b, paint);
        resumeR = new Rect(a, b, a + resume.getWidth(), b + resume.getHeight());

        home = scaleDown(home, (float) w / 4, true);
        a = screenX / 2 - home.getWidth()/2;
        b = (int) (screenY / 2 + y * 0.1);
        canvas.drawBitmap(home, a, b, paint);
        homeR = new Rect(a, b, a + home.getWidth(), b + home.getHeight());

        newGame = scaleDown(newGame, (float) w / 2, true);
        a = screenX / 2 - newGame.getWidth()/2;
        b = (int) (screenY / 2 + y * 0.6);
        canvas.drawBitmap(newGame, a, b, paint);
        newGameR = new Rect(a, b, a + newGame.getWidth(), b + newGame.getHeight());
    }


    /** Draws the game over screen and its options */
    private void drawGameOver() {
        // draw white background
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_background);
        Bitmap bitmap2 = scaleDown(bitmap, (float) screenX - 300, true);
        double h = bitmap2.getHeight();
        double w = bitmap2.getWidth();
        int x = screenX / 2 - (int) w / 2;
        int y = screenY / 2 - (int) (h * 0.6);
        paint = new Paint();
        canvas.drawBitmap(bitmap2, x, y, paint);

        // get all the bitmaps
        Bitmap gameover = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_title);
        Bitmap yourScore = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_your_score);
        Bitmap home = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_home);
        Bitmap playAgain = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.game_over_play_again);

        gameover = scaleDown(gameover, (float) w - 100, true);
        canvas.drawBitmap(gameover, x + 40, y + 40, paint);
        yourScore = scaleDown(yourScore, (float) (w / 1.8), true);
        canvas.drawBitmap(yourScore, screenX / 2 - yourScore.getWidth() / 2,
                (float) (screenY / 2 - h * 0.3), paint);

        home = scaleDown(home, (float) w / 5, true);
        float a = screenX / 2 - home.getWidth() / 2;
        float b = (float) (screenY / 2 + h * 0.2);
        canvas.drawBitmap(home, a, b, paint);
        homeR = new Rect((int) a, (int) b, (int) (a + home.getWidth()), (int) (b + home.getHeight()));

        playAgain = scaleDown(playAgain, (float) w / 2, true);
        a = screenX / 2 - playAgain.getWidth() / 2;
        b = (float) (screenY / 2 + h * 0.05);
        canvas.drawBitmap(playAgain, a, b, paint);
        playAgainR = new Rect((int) a, (int) b, (int) (a + playAgain.getWidth()), (int) (b + playAgain.getHeight()));

        // draw the current score
        paint.setColor(Color.argb(255, 255, 192, 29));
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(Integer.toString(score), screenX / 2,
                (float) (screenY / 2 - h * 0.08), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }


    /** Helper method to scale bitmaps */
    private Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }




    /** The SurfaceView class implements onTouchListener, so we can override this method and
     * detect screen touches
     * @param motionEvent some motion
     * @return true
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Our code here
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (motionEvent.getActionMasked()) {
            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                // if game over, return to main screen
                if (gameOver) {
                    if (MotionEvent.ACTION_DOWN == 0) {
                        if (tap(x, y, homeR)) {
                            context.startActivity(new Intent(context, MainActivity.class));
                        } else if (tap(x, y, playAgainR)) {
                            newGame();
                        }
                    }
                } else {
                    if (tap(x, y, pauseBtnR)) {
                        paused = true;
                        pauseMenu = true;
                    } else if (pauseMenu) {
                        if (tap(x, y, resumeR)) {
                            pauseMenu = false;
                            paused = false;
                        } else if (tap(x, y, homeR)) {
                            context.startActivity(new Intent(context, MainActivity.class));
                        } else if (tap(x, y, newGameR)) {
                            pauseMenu = false;
                            newGame();
                        }
                    } else {
                        paused = false;
                        touching = true;
                        if (motionEvent.getX() > screenX / (float) 2) {
                            paddle.setMovementState(paddle.RIGHT);
                        } else {
                            paddle.setMovementState(paddle.LEFT);
                        }
                        break;
                    }
                }

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                touching = false;
                speed = 100;
                paddle.setMovementState(paddle.STOPPED);
                break;
        }

        return true;
    }


    /** helper method to see if something is tapped */
    private boolean tap(float x, float y, Rect r) {
        if (x >= r.left && x < r.right && y >= r.top && y < r.bottom)
            return true;
        return false;
    }
}
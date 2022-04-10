package com.honeybadger.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable{

    private Thread mThread = null;
    private long mNextFrameTime;
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    //sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    //the size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    private int mScore;

    // Drawing objects
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    private Snake mSnake;
    private Apple mApple;

    public SnakeGame(Context context, Point size) {
        super(context);

        //amount of pixels in each block
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        //and how many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;

        //nobody likes the annoying sound, ain't it?
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //    AudioAttributes audioAttributes = new AudioAttributes.Builder()
        //            .setUsage(AudioAttributes.USAGE_MEDIA)
        //            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        //            .build();
        //
        //    mSP = new SoundPool.Builder()
        //            .setMaxStreams(5)
        //            .setAudioAttributes(audioAttributes)
        //            .build();
        //} else {
        //    mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        //}
        //try {
        //    AssetManager assetManager = context.getAssets();
        //    AssetFileDescriptor descriptor;
        //
        //    // Prepare the sounds in memory
        //    descriptor = assetManager.openFd("get_apple.ogg");
        //    mEat_ID = mSP.load(descriptor, 0);
        //
        //    descriptor = assetManager.openFd("snake_death.ogg");
        //    mCrashID = mSP.load(descriptor, 0);
        //
        //} catch (IOException e) {
        //    // Error
        //}

        //init the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

    }

    public void newGame() {

        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);
        mApple.spawn();
        mScore = 0;
        mNextFrameTime = System.currentTimeMillis();
    }


    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused) {
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        final long TARGET_FPS = 7;
        final long MILLIS_PER_SECOND = 1000;

        if(mNextFrameTime <= System.currentTimeMillis()){
            mNextFrameTime = System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;
            return true;
        }
        return false;
    }


    //redraw all the game objects
    public void update() {

        mSnake.move();

        if(mSnake.checkDinner(mApple.getLocation())){
            mApple.spawn();
            mScore = mScore + 1;

            //nobody!
            //mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        if (mSnake.detectDeath()) {
            mPaused = true;
        }

    }


    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            mCanvas = mSurfaceHolder.lockCanvas();

            //set the background color
            mCanvas.drawColor(Color.argb(255, 60, 30, 80));

            //text color
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setTextSize(40);

            mCanvas.drawText("" + mScore, 20, 120, mPaint);

            mApple.draw(mCanvas, mPaint);
            mSnake.draw(mCanvas, mPaint);

            if(mPaused){
                mPaint.setColor(Color.argb(255, 255, 255, 255));
                mPaint.setTextSize(110);

                mCanvas.drawText(getResources().
                                getString(R.string.tap_to_play),
                        200, 700, mPaint);
            }


            //unlock the mCanvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused) {
                    mPaused = false;
                    newGame();

                    return true;
                }

                mSnake.switchHeading(motionEvent);
                break;

            default:
                break;
        }
        return true;
    }


    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.d("SnakeGame", e.toString());
        }
    }

    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}

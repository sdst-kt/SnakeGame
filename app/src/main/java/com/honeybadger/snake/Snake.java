package com.honeybadger.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

public class Snake {
    private ArrayList<Point> segmentLocations;
    private int mSegmentSize;
    private Point mMoveRange;
    private int halfWayPoint;
    private enum Heading{
        UP, RIGHT, DOWN, LEFT
    }
    private Heading heading = Heading.RIGHT;
    private Bitmap mBitmapHeadRight;
    private Bitmap mBitmapHeadLeft;
    private Bitmap mBitmapHeadUp;
    private Bitmap mBitmapHeadDown;
    private Bitmap mBitmapBody;

    Snake(Context context, Point mr, int ss){
        segmentLocations = new ArrayList<>();
        mSegmentSize = ss;
        mMoveRange = mr;

        mBitmapHeadRight= BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadUp   = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);
        mBitmapHeadDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        mBitmapHeadRight= Bitmap.createScaledBitmap(mBitmapHeadRight,ss,ss,false);

        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);

        mBitmapHeadLeft = Bitmap.createBitmap(mBitmapHeadRight, 0,0, ss, ss, matrix, true);

        matrix.preRotate(-90);
        mBitmapHeadUp   = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);

        matrix.preRotate(180);
        mBitmapHeadDown = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);

        mBitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.body);
        mBitmapBody = Bitmap.createScaledBitmap(mBitmapBody, ss, ss, false);

        halfWayPoint = mr.x * ss / 2;
    }

    void reset(int w, int h){
        heading = Heading.RIGHT;
        segmentLocations.clear();
        segmentLocations.add(new Point(w/2, h/2));
    }

    void move(){
        for(int i = segmentLocations.size() - 1; i > 0; i--){
            segmentLocations.get(i).x = segmentLocations.get(i - 1).x;
            segmentLocations.get(i).y = segmentLocations.get(i - 1).y;
        }

        //move the head in the appropriate heading
        //get the existing head position:
        Point p = segmentLocations.get(0);

        //and let's move it:
        switch(heading){
            case UP:
                p.y--;
                break;
            case RIGHT:
                p.x++;
                break;
            case DOWN:
                p.y++;
                break;
            case LEFT:
                p.x--;
                break;
        }
//        segmentLocations.set(0, p);
    }

    boolean detectDeath(){
        boolean dead = segmentLocations.get(0).x == -1 ||
                segmentLocations.get(0).x == mMoveRange.x ||
                segmentLocations.get(0).y == -1 ||
                segmentLocations.get(0).y == mMoveRange.y;

        //autocannibalism detected:
        for(int i = segmentLocations.size() - 1; i > 0; i--){
            if (
                    segmentLocations.get(0).x == segmentLocations.get(i).x &&
                            segmentLocations.get(0).y == segmentLocations.get(i).y
            ) {
                dead = true;
                break;
            }
        }
        if(dead) {
            Log.d("SnakeGame", "===>>> death; y: " + segmentLocations.get(0).x);
            Log.d("SnakeGame", "===>>> death: x: " + segmentLocations.get(0).y);
        }

        return dead;
    }

    boolean checkDinner(Point l){
        if(
            segmentLocations.get(0).x == l.x &&
            segmentLocations.get(0).y == l.y
        ){
            segmentLocations.add(new Point(-10, -10));
            return true;
        }
        return false;
    }

    void draw(Canvas canvas, Paint paint){
        if(!segmentLocations.isEmpty()){
            //drawing head:
            switch (heading){
                case RIGHT:
                    canvas.drawBitmap(
                            mBitmapHeadRight,
                            segmentLocations.get(0).x * mSegmentSize,
                            segmentLocations.get(0).y * mSegmentSize,
                            paint
                    );
                    break;
                case LEFT:
                    canvas.drawBitmap(
                            mBitmapHeadLeft,
                            segmentLocations.get(0).x * mSegmentSize,
                            segmentLocations.get(0).y * mSegmentSize,
                            paint
                    );
                    break;
                case UP:
                    canvas.drawBitmap(
                            mBitmapHeadUp,
                            segmentLocations.get(0).x * mSegmentSize,
                            segmentLocations.get(0).y * mSegmentSize,
                            paint
                    );
                    break;
                case DOWN:
                    canvas.drawBitmap(
                            mBitmapHeadDown,
                            segmentLocations.get(0).x * mSegmentSize,
                            segmentLocations.get(0).y * mSegmentSize,
                            paint
                    );
                    break;
            }

            //body:
            for(int i = 1; i < segmentLocations.size(); i++){
                canvas.drawBitmap(
                        mBitmapBody,
                        segmentLocations.get(i).x * mSegmentSize,
                        segmentLocations.get(i).y * mSegmentSize,
                        paint
                );
            }

            Log.d("x: ", "" + segmentLocations.get(0).x);
            Log.d("y: ", "" + segmentLocations.get(0).y);
        }
    }

    //rotation
    void switchHeading(MotionEvent motionEvent){
        if(motionEvent.getX() >= halfWayPoint) {
            //right:
            switch (heading) {
                case UP:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.UP;
                    break;
            }
        } else {
            //left:
            switch (heading){
                case UP:
                    heading = Heading.LEFT;
                    break;
                case LEFT:
                    heading = Heading.DOWN;
                    break;
                case DOWN:
                    heading = Heading.RIGHT;
                    break;
                case RIGHT:
                    heading = Heading.UP;
                    break;
            }
        }
    }
}
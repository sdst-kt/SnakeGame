package com.honeybadger.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SnakeActivity extends AppCompatActivity {
    SnakeGame mSnakeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mSnakeGame = new SnakeGame(this, size);
        setContentView(mSnakeGame);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSnakeGame.resume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSnakeGame.pause();
    }
}

package com.yeloser.yeloserserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by brayskiy on 5/5/15.
 */


public class GameView extends View {

    private boolean mGameRunning = true;

    private Bitmap mSoccerBall = null;
    private Bitmap mBrick = null;

    private int dX = -2;
    private int dY = -2;
    private int ballX = 100;
    private int ballY = 100;

    private int brickX = 0;
    private int brickY = 0;
    private int brickWidth = 100;
    private int brickHeight = 25;

    private float mAX = 0f;

    private RectF mBrickRect = new RectF();
    private static Paint mRedPaint = new Paint();

    static {
        mRedPaint.setColor(Color.RED);
        mRedPaint.setStyle(Paint.Style.FILL);
    }

    // METHODS

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.soccer_ball);
        int dim = 50;//Math.min(getWidth(), getHeight()) / 20;
        mSoccerBall = Bitmap.createScaledBitmap(bitmap, dim, dim, false);
    }

    public void setAcelerometerData(float aX) {
        mAX = aX;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mGameRunning) {
            if (ballY > (getHeight() - mSoccerBall.getHeight())) {
                mGameRunning = false;

                brickX = (getWidth() - brickWidth) / 2;
                brickY = getHeight() - 30;
                mBrickRect.set(brickX, brickY, brickX + brickWidth, brickY + brickHeight);

                ballX = getWidth() / 2 + mSoccerBall.getWidth() / 2;
                ballY = getHeight() - mSoccerBall.getHeight() - brickHeight - 1;
                dX = -2;
                dY = -2;
            } else {
                if (((brickX < (getWidth() - brickWidth)) && (mAX > 0)) || ((brickX > 0) && (mAX < 0))) {
                    brickX += (int) mAX;
                }
                brickY = getHeight() - 30;

                mBrickRect.set(brickX, brickY, brickX + brickWidth, brickY + brickHeight);

                if ((ballX > (getWidth() - mSoccerBall.getWidth())) || (ballX < 0)) dX = -dX;
                if (ballY < 0) dY = -dY;

                // Brick interaction
                if (
                        (ballX < (brickX - mSoccerBall.getWidth() / 2 + brickWidth)) &&
                        (ballX > (brickX - mSoccerBall.getWidth() / 2)) &&
                                (ballY > (brickY - mSoccerBall.getHeight()))) {
                    dY = -dY;
                }

                ballX += dX;
                ballY += dY;
            }
        } else {
            // TODO
        }

        canvas.drawRoundRect(mBrickRect, 4, 4, mRedPaint);
        canvas.drawBitmap(mSoccerBall, ballX, ballY, new Paint());
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            // TODO
            //mouseClicked(new Point(e.getX(), e.getY()));

            if (!mGameRunning) {
                mGameRunning = true;
            }
        }

        return super.onTouchEvent(e);
    }

} // class GameView

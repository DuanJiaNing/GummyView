package com.duan.gummyviewdemo.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.duan.gummyviewdemo.bezier.BezierImpl;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */
//FIXME
public class GummySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GummySurfaceView";
    private SurfaceHolder mHolder;

    private SurfaceDrawThread mDrawThread;

    private Gummy gummy;

    private final int START_SPIN = 1;

    private final int STOP_SPIN = 1;

    {
        Log.i(TAG, "init{}: thread="+Thread.currentThread().getName());
        mHolder = this.getHolder();
        mHolder.addCallback(this);
    }

    public GummySurfaceView(Context context) {
        super(context);
    }

    public GummySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GummySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        this.setZOrderOnTop(true);

        gummy = new Gummy(this, new BezierImpl());
        gummy.setLot(20);
        gummy.setColor(Color.BLUE);
//        gummy.setAngleOffStart((float) (Math.PI / 4)); // 45 度
        gummy.setCenterX(550.0f);
        gummy.setCenterY(800.0f);
        gummy.setOutLineLengthForAll(120.0f);
        gummy.setRadius(200.0f);
        gummy.setInnerLineLengthForAll(0.0f);

//        gummy.setOutLineLength(0, new float[]{150.0f, 150.0f, -30.0f, 30.0f, 200.0f, 100.0f});
//        gummy.setAngle(Math.PI / 2, 0);

        mDrawThread = new SurfaceDrawThread();
        mDrawThread.start();
        try {
            mDrawThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mDrawThread.getLooper().quitSafely();
        } else
            mDrawThread.getLooper().quit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDrawThread.startSpin();
        return true;
    }

    private class DrawHandler extends Handler {

        private ValueAnimator rotateAnim;

        public DrawHandler(Looper looper) {
            super(looper);
            rotateAnim = ObjectAnimator.ofFloat(gummy, "angleOffStart", 0, (float) Math.PI * 2);
            rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnim.setRepeatMode(ValueAnimator.REVERSE);
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mDrawThread.draw();
                }
            });
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SPIN:
                    Log.i(TAG, "handleMessage: thread="+Thread.currentThread().getName());
                    rotateAnim.start();
                    break;
            }
        }
    }

    private class SurfaceDrawThread extends Thread {

        private DrawHandler handler;

        private Canvas mCanvas;

        private Paint mPaint;

        private Looper mLooper;

        public SurfaceDrawThread() {
            mPaint = new Paint();
            Log.i(TAG, "SurfaceDrawThread: thread="+Thread.currentThread().getName());
        }

        @Override
        public void run() {
            Log.i(TAG, "run: thread="+Thread.currentThread().getName());
            Looper.prepare();
            mLooper = Looper.myLooper();
            handler = new DrawHandler(Looper.myLooper());
            draw();
            Looper.loop();
        }

        public void draw() {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);

            float x0, y0, x, y;

            //计算出所有的控制点
            float[][] points = gummy.calcuCoordinates();

            //计算出贝塞尔曲线上的点并绘制
            float[][] pos = gummy.calcuBeziers(points, 200);
            gummy.drawBeziers(mCanvas, mPaint, pos);

            //绘制连接控制点的线
            x0 = points[0][0];
            y0 = points[0][1];
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(5.0f);
            for (int i = 1; i < points.length; i++) {
                x = points[i][0];
                y = points[i][1];
                mCanvas.drawLine(x0, y0, x, y, mPaint);
                x0 = x;
                y0 = y;
            }

            //绘制圆
            mPaint.setColor(Color.BLACK);
            mPaint.setAlpha(50); //要在 setColor 后调用，否则无效
            mCanvas.drawCircle(gummy.getCenterX(), gummy.getCenterY(), gummy.getRadius(), mPaint);

            //绘制过圆心的两条线
            mPaint.setColor(Color.GRAY);
            mPaint.setAlpha(100);
            mCanvas.drawLine(gummy.getCenterX() - gummy.getRadius(), gummy.getCenterY(),
                    gummy.getCenterX() + gummy.getRadius(), gummy.getCenterY(), mPaint);
            mCanvas.drawLine(gummy.getCenterX(), gummy.getCenterY() - gummy.getRadius(),
                    gummy.getCenterX(), gummy.getCenterY() + gummy.getRadius(), mPaint);

            mHolder.unlockCanvasAndPost(mCanvas);
        }

        public void startSpin() {
            Log.i(TAG, "startSpin: thread="+Thread.currentThread().getName());
            handler.sendEmptyMessage(START_SPIN);
        }

        public Looper getLooper() {
            return mLooper;
        }
    }

}

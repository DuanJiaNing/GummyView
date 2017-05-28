package com.duan.gummyviewdemo.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.duan.gummyviewdemo.bezier.BezierImpl;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class GummySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;

    private SurfaceDrawThread mDrawThread;

    private boolean hasRunning = false;

    private Gummy gummy;

    private ValueAnimator rotateAnim;

    {
        //获得持有者
        mHolder = this.getHolder();

        //注册功能
        mHolder.addCallback(this);

        setLongClickable(true);

        gummy = new Gummy(GummySurfaceView.this, new BezierImpl());

        mDrawThread = new SurfaceDrawThread();


        rotateAnim = ObjectAnimator.ofFloat(0, (float) Math.PI * 2);
        rotateAnim.setDuration(3000);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);


    }

    public GummySurfaceView(Context context) {
        super(context);
    }

    public GummySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GummySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GummySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        hasRunning = true;
        mDrawThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasRunning = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);

    }

    private class SurfaceDrawThread extends Thread {

        private Canvas mCanvas;

        private Paint mPaint;

        /**
         * 每30帧刷新一次屏幕
         **/
        public static final int TIME_IN_FRAME = 30;

        public SurfaceDrawThread() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);

            init();
        }

        private void init() {
            gummy.setLot(31);
            gummy.setColor(Color.BLUE);
//            gummy.setAngleOffStart(Math.PI / 4); // 45 度
            gummy.setCenterX(350.0f);
            gummy.setCenterY(500.0f);
            gummy.setOutLineLengthForAll(60.0f);
            gummy.setRadius(150.0f);

//        gummy.setOutLineLength(0, new float[]{150.0f, 150.0f, -30.0f, 30.0f, 200.0f, 100.0f});
//        gummy.setAngle(Math.PI / 2, 0);

            gummy.setOnDrawBezier(new Gummy.OnDrawBezier() {
                @Override
                public void drawBezier(Canvas canvas, Paint paint, float[][] points) {
                    paint.setColor(Color.RED);
                    paint.setStrokeWidth(10.0f);

                    float x0 = points[0][0], y0 = points[0][1], x, y;
                    Path path = new Path();
                    path.moveTo(x0, y0);
                    for (int i = 1; i < points.length; i++) {
                        x = points[i][0];
                        y = points[i][1];
//                        canvas.drawLine(x0, y0, x, y, paint);
                        path.lineTo(x, y);
                        x0 = x;
                        y0 = y;
                    }
                    canvas.drawPath(path, paint);
                }
            });

        }

        @Override
        public void run() {
            Looper.prepare();
            Looper.loop();
//            rotateAnim.start();

            while (hasRunning) {

                /**取得更新游戏之前的时间**/
                long startTime = System.currentTimeMillis();

                mCanvas = mHolder.lockCanvas();
                float[][] points = gummy.calcuCoordinates();
//                gummy.calcuBeziers(points, 1000, mCanvas, mPaint);
                mHolder.unlockCanvasAndPost(mCanvas);

                /**取得更新游戏结束的时间**/
                long endTime = System.currentTimeMillis();

                /**计算出游戏一次更新的毫秒数**/
                int diffTime = (int) (endTime - startTime);

//                while (diffTime <= TIME_IN_FRAME) {
//                    diffTime = (int) (System.currentTimeMillis() - startTime);
//                    /**线程等待**/
//                    Thread.yield();
//                }


            }


        }


    }


}

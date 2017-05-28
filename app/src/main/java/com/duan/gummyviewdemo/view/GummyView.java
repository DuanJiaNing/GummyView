package com.duan.gummyviewdemo.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;

import com.duan.gummyviewdemo.bezier.BezierImpl;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class GummyView extends View {

    private Paint paint;

    private Gummy gummy;

    private ValueAnimator rotateAnim;

    private ValueAnimator colorAnim;

    private ValueAnimator radiusAnim;

    private ValueAnimator outLineLengthAnim;

    private ValueAnimator innerLineLengthAnim;

    {
        paint = new Paint();
        paint.setAntiAlias(true);
        gummy = new Gummy(this, new BezierImpl());
        gummy.setOnDrawBezier(new Gummy.OnDrawBezier() {
            Path path = new Path();

            @Override
            public void drawBezier(Canvas canvas, Paint paint, float[][] points) {
                paint.setColor(gummy.getColor());
                paint.setStrokeWidth(10.0f);

                float x, y;
                path.reset();
                path.moveTo(points[0][0], points[0][1]);
                for (int i = 1; i < points.length; i++) {
                    x = points[i][0];
                    y = points[i][1];
                    path.lineTo(x, y);
                }
                canvas.drawPath(path, paint);
            }
        });

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


        rotateAnim = ObjectAnimator.ofFloat(gummy, "angleOffStart", 0, (float) Math.PI * 2);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setRepeatMode(ValueAnimator.REVERSE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAnim = ObjectAnimator.ofArgb(gummy, "color", Color.BLUE, Color.GREEN, Color.BLACK);
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        }

        float ra = gummy.getRadius();
        radiusAnim = ObjectAnimator.ofFloat(gummy, "radius", ra, ra * 2, ra);
        radiusAnim.setRepeatCount(ValueAnimator.INFINITE);
        radiusAnim.setRepeatMode(ValueAnimator.REVERSE);

        float ol = 120.0f;
        outLineLengthAnim = ObjectAnimator.ofFloat(ol, -ol, ol);
        outLineLengthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                gummy.setOutLineLengthForAll(f);
            }
        });
        outLineLengthAnim.setRepeatCount(ValueAnimator.INFINITE);
        outLineLengthAnim.setRepeatMode(ValueAnimator.REVERSE);

        float il = 0.0f;
        innerLineLengthAnim = ObjectAnimator.ofFloat(il, ra * 2 * 2 , il);
        innerLineLengthAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float f = (float) animation.getAnimatedValue();
                gummy.setInnerLineLengthForAll(f);
            }
        });
        innerLineLengthAnim.setRepeatCount(ValueAnimator.INFINITE);
        innerLineLengthAnim.setRepeatMode(ValueAnimator.REVERSE);


//        rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float v = (float) animation.getAnimatedValue();
//                gummy.setAngleOffStart(v);
//            }
//        });

    }

    public GummyView(Context context) {
        super(context);
    }

    public GummyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        float x0, y0, x, y;

        //计算出所有的控制点
        float[][] points = gummy.calcuCoordinates();

        //计算出贝塞尔曲线上的点并绘制
        float[][] pos = gummy.calcuBeziers(points, 200);
        gummy.drawBeziers(canvas, paint, pos);


//
//        //绘制连接控制点的线
//        x0 = points[0][0];
//        y0 = points[0][1];
//        paint.setColor(Color.RED);
//        paint.setStrokeWidth(5.0f);
//        for (int i = 1; i < points.length; i++) {
//            x = points[i][0];
//            y = points[i][1];
//            canvas.drawLine(x0, y0, x, y, paint);
//            x0 = x;
//            y0 = y;
//        }
//
//        //绘制圆
//        paint.setColor(Color.BLACK);
//        paint.setAlpha(50); //要在 setColor 后调用，否则无效
//        canvas.drawCircle(gummy.getCenterX(), gummy.getCenterY(), gummy.getRadius(), paint);
//
//        //绘制过圆心的两条线
//        paint.setColor(Color.GRAY);
//        paint.setAlpha(100);
//        canvas.drawLine(gummy.getCenterX() - gummy.getRadius(), gummy.getCenterY(),
//                gummy.getCenterX() + gummy.getRadius(), gummy.getCenterY(), paint);
//        canvas.drawLine(gummy.getCenterX(), gummy.getCenterY() - gummy.getRadius(),
//                gummy.getCenterX(), gummy.getCenterY() + gummy.getRadius(), paint);


    }

    private int which = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            AnimatorSet set = new AnimatorSet();
            set.setDuration(8000);
            set.play(rotateAnim)
                    .with(colorAnim)
                    .with(radiusAnim)
                    .with(outLineLengthAnim)
                    .with(innerLineLengthAnim);
            set.start();
            return false;
        }

//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN: {
//                float ra = 30;
//                float re = 0.00001f;
//
//                which = -1;
//                float cx, cy;
//                for (int i = 0; i < points.length; i++) {
//                    cx = points[i][0];
//                    cy = points[i][1];
//
//                    if (x - (cx - ra) > re && x - (cx + ra) < re
//                            && y - (cy - ra) > re && y - (cy + ra) < re) {
//                        which = i;
//                        break;
//                    }
//                }
//                break;
//            }
//            case MotionEvent.ACTION_UP:
//                which = -1;
//            default:
//                if (which != -1) {
//                    points[which][0] = x;
//                    points[which][1] = y;
//                    invalidate();
//                }
//        }
//
//
        return true;
    }

}

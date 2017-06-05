package com.duan.gummyviewdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.duan.gummyviewdemo.R;
import com.duan.gummyviewdemo.bezier.BezierImpl;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class GummyView extends View {

    private static final String LOG_TAG = "GummyView";
    private Paint mPaint;
    private Gummy mGummy;

    private int mLot;
    private int mRadius;
    private int mColor;
    private int mOutPointDistance;

    public GummyView(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GummyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public GummyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initGummy();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GummyView, defStyleAttr, defStyleRes);

        mLot = array.getInt(R.styleable.GummyView_lot, mGummy.getLot());
        mRadius = array.getDimensionPixelSize(R.styleable.GummyView_radius, (int) mGummy.getRadius());
        mColor = array.getColor(R.styleable.GummyView_color, mGummy.getColor());
        mOutPointDistance = array.getDimensionPixelSize(R.styleable.GummyView_outPointDistance, (int) mGummy.getOutPointDistanceFromCircleBorder()[0]);

        mGummy.setLot(mLot);
        mGummy.setRadius(mRadius);
        mGummy.setColor(mColor);
        mGummy.setOutLineLengthForAll(mOutPointDistance);

        array.recycle();
    }

    private void initGummy() {
        mGummy = new Gummy(this, new BezierImpl());
        mGummy.setLot(16);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int wss = MeasureSpec.getSize(widthMeasureSpec);
        int wsm = MeasureSpec.getMode(widthMeasureSpec);
        int hss = MeasureSpec.getSize(heightMeasureSpec);
        int hsm = MeasureSpec.getMode(heightMeasureSpec);

        int measuredWidth = wss;
        int measuredHeight = hss;

        float max = 0;
        float[] lens = mGummy.getOutPointDistanceFromCircleBorder();
        for (float f : lens) {
            max = (f - max) > 0.00000001 ? f : max;
        }
        if (wsm == MeasureSpec.AT_MOST) {
            int ra = (int) (mGummy.getRadius() + max) * 2 + 20;
            measuredWidth = getPaddingLeft() + getPaddingRight() + ra;
            measuredHeight = getPaddingTop() + getPaddingBottom() + ra;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mGummy.setCenterX(getWidth() / 2);
        mGummy.setCenterY(getHeight() / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float x0, y0, x, y;

        //计算出所有的控制点
        float[][] points = mGummy.calcuCoordinates();

        canvas.save();
        canvas.rotate((float) Math.toRadians(mGummy.getAngleOffStart()));

        //计算出贝塞尔曲线上的点并使用默认的绘制方法绘制
        float[][] pos = mGummy.calcuBeziers(points, 200);
        mGummy.drawBeziers(canvas, mPaint, pos);

        canvas.restore();

//
//        //绘制连接控制点的线
//        x0 = points[0][0];
//        y0 = points[0][1];
//        mPaint.setColor(Color.RED);
//        mPaint.setStrokeWidth(5.0f);
//        for (int i = 1; i < points.length; i++) {
//            x = points[i][0];
//            y = points[i][1];
//            canvas.drawLine(x0, y0, x, y, mPaint);
//            x0 = x;
//            y0 = y;
//        }
//
//        //绘制圆
//        mPaint.setColor(Color.BLACK);
//        mPaint.setAlpha(50); //要在 setColor 后调用，否则无效
//        canvas.drawCircle(mGummy.getCenterX(), mGummy.getCenterY(), mGummy.getRadius(), mPaint);
//
//        //绘制过圆心的两条线
//        mPaint.setColor(Color.GRAY);
//        mPaint.setAlpha(100);
//        canvas.drawLine(mGummy.getCenterX() - mGummy.getRadius(), mGummy.getCenterY(),
//                mGummy.getCenterX() + mGummy.getRadius(), mGummy.getCenterY(), mPaint);
//        canvas.drawLine(mGummy.getCenterX(), mGummy.getCenterY() - mGummy.getRadius(),
//                mGummy.getCenterX(), mGummy.getCenterY() + mGummy.getRadius(), mPaint);
//

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}

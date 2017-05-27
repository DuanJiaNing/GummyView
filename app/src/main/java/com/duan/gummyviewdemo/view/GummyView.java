package com.duan.gummyviewdemo.view;

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

import com.duan.gummyviewdemo.bezier.BezierImpl;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class GummyView extends View {

    private Paint paint;

    private Gummy gummy;

    private BezierOctopus bezier;

    private float[][] points;

    {
        paint = new Paint();
        paint.setAntiAlias(true);
        gummy = new Gummy(this);
        bezier = new BezierOctopus();
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        gummy.setLot(16);
        gummy.setColor(Color.BLUE);
//        octopus.setAngleOffStart(Math.PI / 4); // 45 度
        gummy.setCenterX(350.0f);
        gummy.setCenterY(500.0f);
        gummy.setOutLineLengthForAll(60.0f);
        gummy.setRadius(150.0f);

//        octopus.setOutLineLength(0, new float[]{150.0f, 150.0f, -30.0f, 30.0f, 200.0f, 100.0f});
//        octopus.setAngle(Math.PI / 2, 0);

        //计算“章鱼”的各个折点坐标
        points = gummy.calcuCoordinates();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        float x0, y0, x, y;
//
//        //画圆
//        paint.setColor(Color.GRAY);
//        canvas.drawCircle(octopus.getCenterX(), octopus.getCenterY(), octopus.getRadius(), paint);
//
//        //画过圆心的两条线
//        paint.setColor(Color.WHITE);
//        canvas.drawLine(octopus.getCenterX() - octopus.getRadius(), octopus.getCenterY(),
//                octopus.getCenterX() + octopus.getRadius(), octopus.getCenterY(), paint);
//        canvas.drawLine(octopus.getCenterX(), octopus.getCenterY() - octopus.getRadius(),
//                octopus.getCenterX(), octopus.getCenterY() + octopus.getRadius(), paint);
//
//        paint.setColor(Color.BLACK);
//        paint.setStrokeWidth(3.0f);
//        for (int i = 0; i < points.length; i++) {
//            canvas.drawCircle(points[i][0], points[i][1], 5, paint);
//        }
//

//        x0 = lines[0][0];
//        y0 = lines[0][1];
//        for (int i = 1; i < lines.length; i++) {
//            x = lines[i][0];
//            y = lines[i][1];
//            canvas.drawLine(x0, y0, x, y, paint);
//            x0 = x;
//            y0 = y;
//        }

        //画贝塞尔曲线

        paint.setColor(gummy.getColor());
        bezier.drawBeziers(canvas, paint, points);


    }

    private int which = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float ra = 30;
                float re = 0.00001f;

                which = -1;
                float cx, cy;
                for (int i = 0; i < points.length; i++) {
                    cx = points[i][0];
                    cy = points[i][1];

                    if (x - (cx - ra) > re && x - (cx + ra) < re
                            && y - (cy - ra) > re && y - (cy + ra) < re) {
                        which = i;
                        break;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
                which = -1;
            default:
                if (which != -1) {
                    points[which][0] = x;
                    points[which][1] = y;
                    invalidate();
                }
        }


        return true;
    }


    private class BezierOctopus extends BezierImpl {

        //分段绘制贝塞尔曲线，如果直接用所有点作为控制点绘制会导致起点和终点无法自然闭合
        public void drawBeziers(Canvas canvas, Paint paint, float[][] points) {

            int count = points.length;

            //绘制次数（ 块 数）
            int rest = gummy.getDiv();

            // 定位到第一个 圆外两点之间的点 ，即第五个点。
            //选择 圆外两点之间的点 到相邻的 圆外两点之间的点 作为一次绘制单位
            int curPoint = 4;

            //一次绘制9个点（9个控制点）
            int drawOfTime = 9;

            path.moveTo(points[curPoint][0], points[curPoint][1]);

            while (rest != 0) {

                float[][] curDraw = new float[9][2];
                for (int i = 0; i < drawOfTime; i++) {
                    if (curPoint == count - 1)
                        curPoint = 0;
                    curDraw[i] = points[curPoint];
                    curPoint++;
                }
                //下一次绘制时起点往前移一个
                curPoint--;
                drawBezier(paint, canvas, curDraw);

                rest--;
            }

//            path.close();
            canvas.drawPath(path, paint);

        }

        /**
         * 绘制贝塞尔曲线（控制点数不限）
         */
        private Path path = new Path();

        private void drawBezier(Paint paint, Canvas canvas, float[][] controlPoints) {
            float x0, y0, x, y;
            int precision = 200;
            float[][] ps = calculate(controlPoints, precision);
//            x0 = ps[0][0];
//            y0 = ps[0][1];
            for (int i = 1; i < ps.length; i++) {
                x = ps[i][0];
                y = ps[i][1];
                path.lineTo(x, y);
//                canvas.drawLine(x0, y0, x, y, paint);
//                x0 = x;
//                y0 = y;
            }

        }

    }

}

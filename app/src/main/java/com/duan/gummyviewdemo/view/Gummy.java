package com.duan.gummyviewdemo.view;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.view.View;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class Gummy {

    private View view;

    /**
     * 圆心坐标
     */
    private float centerX, centerY;

    /**
     * 圆的半径
     */
    private float radius;

    /**
     * 填充颜色
     */
    private int color = Color.GREEN;

    /**
     * 透明度，0.0 到 1.0
     */
    private float alpha = 1.0f;

    /**
     * 将圆分为几份 (n)，必须为 4 的倍数，且不小于 8 。这样圆将被分为 n/4 块
     * 每计算一块的坐标时将按如下顺序进行：
     * 1.出发点 p0(0,0)
     * 2.第一个在圆上的点 p1
     * 3.第二个在圆上的点 p2
     * 4.第一个圆外延伸的点 a1
     * 5.两圆外延伸点中间的点 b
     * 6.第二个圆外延伸的点 a2
     * 7.第三个在圆上的点 p3
     * 8.第四个在圆上的点 p4
     */
    private int lot;

    /**
     * 保存每一份的角度大小，和应等于 2PI
     */
    private double[] angles;

    /**
     * 当圆被分为 n 份时(lot 值为 n)，圆外延伸的线将有 (n / 4) * 2 条;
     * 每一段圆外延伸线段的长度，该长度将影响圆外延伸点的坐标，其值可分为如下三种情况：
     * 1. =0：在圆上
     * 2. >0：在圆外
     * 3. <0：在圆内
     */
    private float[] outPointDistanceFromCircleBorder;

    private float defaultOutLineLength = 50.0f;

    /**
     * 块数
     */
    private int div;

    public Gummy(View view) {
        this.view = view;
        this.lot = 8;
        this.radius = 50.0f;
        reset(lot);
    }

    /**
     * 该角度值将指定 第一个在圆上的点 p1 的位置（计算各个坐标点时将用到：calcuCoordinates），改变该值可实现旋转控件的效果
     */
    private double angleOffStart = 0;

    private void reset(int l) {
        this.lot = l;

        angles = new double[l];
        double an = 2 * Math.PI / l;
        for (int i = 0; i < l; i++) {
            angles[i] = an;
        }

        outPointDistanceFromCircleBorder = new float[lot / 2];
        for (int i = 0; i < outPointDistanceFromCircleBorder.length; i++) {
            outPointDistanceFromCircleBorder[i] = defaultOutLineLength;
        }

        this.div = lot / 4;

        view.invalidate();
    }

    /**
     * 计算出各个点的坐标，计算时使用控件所在容器的坐标系：即容器左上角为坐标原点，向下为 y 轴正方向，向右为 x 轴正方向。
     */
    public float[][] calcuCoordinates() {

        //端点数目,圆心上有 (lot / 4) + 1 个点（包括起点、终点以及其他中间点）
        //总的坐标点数
        int pointNum = (lot / 4) * 7 + 1 + (lot / 4);

        //平面坐标系
        float[][] result = new float[pointNum][2];

        //标识当前点对应每一块的点
        // 1：起点（圆心）
        // 2：第一个在圆上的点 p1
        // 3：第二个在圆上的点 p2
        // 4：第一个圆外延伸的点 a1
        // 5：两圆外延伸点中间的点 b
        // 6：第二个圆外延伸的点 a2
        // 7：第三个在圆上的点 p3
        // 8：第四个在圆上的点 p4
        int currentCoordinate = 1;

        //当前计算到的块
        int currDiv = 0;
        int currOutLine = 0;

        for (int i = 0; i < pointNum; i++) {
            double angle = cacluAngle(currentCoordinate, currDiv);
            switch (currentCoordinate) {
                case 1:
                    result[i][0] = centerX;
                    result[i][1] = centerY;
                    currentCoordinate++;
                    break;
                case 2:
                case 3:
                    result[i] = cacluNode(angle, radius);
                    currentCoordinate++;
                    break;
                case 4:
                    result[i] = cacluNode(angle, radius + outPointDistanceFromCircleBorder[currOutLine]);
                    currOutLine++;
                    currentCoordinate++;
                    break;
                case 5:
                    result[i] = cacluEdge(currDiv, angle);
                    currentCoordinate++;
                    break;
                case 6:
                    result[i] = cacluNode(angle, radius + outPointDistanceFromCircleBorder[currOutLine]);
                    currOutLine++;
                    currentCoordinate++;
                    break;
                case 7:
                    result[i] = cacluNode(angle, radius);
                    currentCoordinate++;
                    break;
                case 8:
                    result[i] = cacluNode(angle, radius);
                    currDiv++;
                    currentCoordinate = 1;
                    break;
            }
//                Log.i("main", "calcuCoordinates: " + i
//                        + "\n(" + result[i][0] + "," + result[i][1] + ")"
//                        + "\nangle=" + angle
//                        + "\ncurrDiv=" + currDiv
//                        + "\ncurrentCoordinate=" + currentCoordinate
//                );
        }

        return result;
    }

    /**
     * 求出直线和圆的交点坐标，使用极坐标求解；
     * 极点：圆心
     * 极轴：过圆心的父容器坐标系 x 轴正方向
     * 极坐标的正方向：父容器坐标系 x 轴正方向指向 y 轴正方向
     * <p>
     * 用到的公式：
     * (x - a)2 + (y - b)2 = r2;
     * cosα2 + sinα2 = 1;
     * 即：
     * x - a = r * cosα
     * y - b = r * sinα
     *
     * @param anglesSum         由 {@link com.duan.gummyviewdemo.view.Gummy#cacluAngle(int, int)} 方法计算得到
     * @param distanceOffCenter 离圆心的距离
     * @return
     */
    private float[] cacluNode(double anglesSum, float distanceOffCenter) {
        float x = (float) (distanceOffCenter * Math.cos(anglesSum) + centerX);
        float y = (float) (distanceOffCenter * Math.sin(anglesSum) + centerY);
        return new float[]{x, y};
    }

    /**
     * 计算每一 块 的圆外两点间的点的坐标
     */
    private float[] cacluEdge(int currDiv, double anglesSum) {
        float frontLineLength = outPointDistanceFromCircleBorder[currDiv * 2];
        float afterLineLength = outPointDistanceFromCircleBorder[currDiv * 2 + 1];
        float distanceOffCenter;
        if (Math.abs(frontLineLength - afterLineLength) < 0.0001)
            distanceOffCenter = frontLineLength + frontLineLength / 5 + radius;
        else {
            distanceOffCenter = Math.min(frontLineLength, afterLineLength) + radius;
            distanceOffCenter += (Math.max(frontLineLength, afterLineLength) - Math.min(frontLineLength, afterLineLength)) / 2;
        }

        return cacluNode(anglesSum, distanceOffCenter);
    }

    /**
     * 计算当前点和圆心相连直线，与极轴的夹角大小
     *
     * @param currentCoordinate 当前点对应每一块的点
     * @return
     */
    private double cacluAngle(int currentCoordinate, int currDiv) {

        double angle = angleOffStart;

        //已经跨过的 块 数
        int so = currDiv * 4;
        switch (currentCoordinate) {
            case 2:
                if (so == 0) {
                    break;
                }

                for (int i = 0; i < so; i++) {
                    angle += angles[i];
                }
                break;
            case 3:
            case 4:
                if (so == 0) {
                    angle += angles[0];
                    break;
                }

                for (int i = 0; i < so + 1; i++) {
                    angle += angles[i];
                }
                break;
            case 5:
                if (so == 0) {
                    angle += angles[0] + angles[1] / 2;
                    break;
                }

                for (int i = 0; i < so + 1; i++) {
                    angle += angles[i];
                }
                angle += angles[so + 1] / 2;
                break;
            case 6:
            case 7:
                if (so == 0) {
                    angle += angles[0] + angles[1];
                    break;
                }

                for (int i = 0; i < so + 2; i++) {
                    angle += angles[i];
                }
                break;
            case 8:
                if (so == 0) {
                    angle += angles[0] + angles[1] + angles[2];
                    break;
                }

                for (int i = 0; i < so + 3; i++) {
                    angle += angles[i];
                }
                break;
        }

        return angle;
    }

    /**
     * 添加一个“块”，这相当于在末尾添加了四个点
     */
    public void addLot(int index) {
        //TODO
    }

    /**
     * 移除一个“块”，这相当于在末尾移除了四个点
     */
    public void removeLot(int index) {
        //TODO
    }

    /**
     * 该角度值用于控制计算“第一个在圆上的点”的坐标与极轴（过圆心的父容器坐标系 x 轴正方向）的偏移量，
     * 在属性动画中改变该值可实现旋转效果
     *
     * @param angleOffStart
     */
    public void setAngleOffStart(double angleOffStart) {
        this.angleOffStart = angleOffStart;
        view.invalidate();
    }

    /**
     * 默认平均分割圆
     * 此方法应谨慎调用，当你重新设定 lot 值，则你之前设置的 angles 和 outPointDistanceFromCircleBorder 都将被重置，可以通过调用
     * {@link com.duan.gummyviewdemo.view.Gummy#addLot(int)} 和 {@link com.duan.gummyviewdemo.view.Gummy#removeLot(int)} 方法避免属性全部被重置
     *
     * @param lot 份数，如果该值不是 4 的倍数或者小于 8，该方法将无效
     */
    public void setLot(int lot) {
        if (this.lot != lot && lot % 4 == 0 && lot >= 8) {
            this.lot = lot;
            reset(lot);
        }
    }

    public void setAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        this.alpha = alpha;
        view.invalidate();
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        view.invalidate();
    }

    /**
     * 修改圆半径，修改该值可实现缩放效果
     *
     * @param radius 半径
     */
    public void setRadius(float radius) {
        this.radius = radius;
        view.invalidate();
    }

    /**
     * 从指定位置开始，修改其圆外延伸线的长度
     *
     * @param startOffSet 起始下标
     * @param lengths     长度
     */
    public void setOutLineLength(int startOffSet, float[] lengths) {

        if (startOffSet < 0 && startOffSet >= outPointDistanceFromCircleBorder.length)
            return;

        int newL = lengths.length + startOffSet;
        int oldL = outPointDistanceFromCircleBorder.length;

        int s = 0;
        if (newL < oldL) {
            for (int i = startOffSet; i < newL; i++) {
                outPointDistanceFromCircleBorder[i] = lengths[s++];
            }
        } else {
            s = 0;
            for (int i = startOffSet; i < oldL; i++) {
                outPointDistanceFromCircleBorder[i] = lengths[s++];
            }
        }

        view.invalidate();
    }

    /**
     * 修改指定位置的圆外延伸线长度
     *
     * @param length 长度
     * @param index  下标
     */
    public void setOutLineLength(float length, int index) {
        if (index < 0 || index >= outPointDistanceFromCircleBorder.length)
            return;
        this.outPointDistanceFromCircleBorder[index] = length;
        view.invalidate();
    }

    /**
     * 将所有圆外延伸线修改为相同值
     *
     * @param length 长度
     */
    public void setOutLineLengthForAll(float length) {
        for (int i = 0; i < outPointDistanceFromCircleBorder.length; i++) {
            outPointDistanceFromCircleBorder[i] = length;
        }
        view.invalidate();
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
        view.invalidate();
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
        view.invalidate();
    }

    /**
     * 为指定下标位置处设置角度大小，如果新角度比原角度大，则大出的部分由其他部份平均分担，如果新角度比原角度小，
     * 则多余的部分由其他部分平均分享。
     */
    public void setAngle(double newAngle, int index) {
        if (index < 0 || index >= angles.length)
            return;

        if (Math.abs(newAngle - angles[index]) <= 0.0001) // 相等
            return;

        double d = newAngle - angles[index];
        angles[index] = newAngle;
        if (d > 0.00001) { //大于
            double e = d / (angles.length - 1);
            for (int i = 0; i < angles.length; i++) {
                if (i != index)
                    angles[i] -= e;
            }

        } else {
            double e = (-d) / (angles.length - 1);
            for (int i = 0; i < angles.length; i++) {
                if (i != index)
                    angles[i] += e;
            }
        }

        view.invalidate();

    }


    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public int getDiv() {
        return div;
    }

    public int getColor() {
        return this.color;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public float getRadius() {
        return radius;
    }

    public int getLot() {
        return lot;
    }

    public double[] getAngles() {
        return angles;
    }

    public float[] getOutPointDistanceFromCircleBorder() {
        return outPointDistanceFromCircleBorder;
    }

    public double getAngle(int index) {
        return angles[index];
    }

    public float getOutPointDistanceFromCircleBorder(int index) {
        return outPointDistanceFromCircleBorder[index];
    }

    public double getAngleOffStart() {
        return angleOffStart;
    }

}

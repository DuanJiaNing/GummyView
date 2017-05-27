package com.duan.gummyviewdemo.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by DuanJiaNing on 2017/5/27.
 */

public class GummySurfaceView extends SurfaceView {
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


}

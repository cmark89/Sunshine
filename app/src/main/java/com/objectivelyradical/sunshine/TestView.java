package com.objectivelyradical.sunshine;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by c.mark on 2015/11/26.
 */
public class TestView extends View {
    private float mAngle = 0;
    private Paint mCirclePaint;
    private Paint mNeedlePaint;

    public TestView(Context c) {
        super(c);
    }
    public TestView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mCirclePaint = new Paint();
        mNeedlePaint = new Paint();

        TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.TestView, 0, 0);
        try {
            mAngle = a.getFloat(R.styleable.TestView_angle, 0f);
        } finally {
            a.recycle();
        }
    }
    public TestView(Context c, AttributeSet attrs, int defaultStyle) {
        super(c, attrs, defaultStyle);
    }
    public void setAngle(float angle) {
        mAngle = angle;
        invalidate();
        requestLayout();
    }
    public float getAngle() {
        return mAngle;
    }

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec) {
        int wSpecMode = MeasureSpec.getMode(wMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(wMeasureSpec);
        int myWidth = 0;
        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);
        int myHeight = 0;
        if(wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else {
            // wrap content... choose own size
            myWidth = 100;
        }
        if(hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else {
            // wrap content... choose own size
            myHeight = 100;
        }
        setMeasuredDimension(myWidth, myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int halfWidth = getMeasuredWidth()/2;
        int halfHeight = getMeasuredHeight()/2;
        int radius = 0;
        if(halfWidth < halfHeight) {
            radius = halfHeight - 10;
        } else {
            radius = halfWidth - 10;
        }
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.BLACK);

        mNeedlePaint.setStyle(Paint.Style.STROKE);
        mNeedlePaint.setStrokeWidth(4);
        mNeedlePaint.setAntiAlias(true);
        mNeedlePaint.setColor(Color.RED);

        float dx, dy;
        dx = (float)Math.cos(mAngle) * radius;
        dy = (float)Math.sin(mAngle) * radius;

        int endX = (int)(halfWidth + dx);
        int endY = (int)(halfHeight + dy);

        canvas.drawCircle(halfWidth, halfHeight, radius, mCirclePaint);
        canvas.drawLine(halfWidth, halfHeight,endX, endY, mNeedlePaint);
    }

}

package com.jeek.calendar.widget.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.jeek.calendar.library.R;

import java.util.Calendar;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public class WeekBarView extends View {

    private int mWeekTextColor;
    private int mWeekSize;
    private Paint mPaint;
    private String[] mWeekString;

    public WeekBarView(Context context) {
        this(context, null);
    }

    public WeekBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeekBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WeekBarView);
        mWeekTextColor = array.getColor(R.styleable.WeekBarView_text_color, Color.parseColor("#4588E3"));
        mWeekSize = array.getDimensionPixelSize(R.styleable.WeekBarView_text_size, context.getResources().getDimensionPixelOffset(R.dimen.size_week_text_size));
        mWeekString = context.getResources().getStringArray(CalendarUtils.weekStart == Calendar.MONDAY ? R.array.calendar_week_monday : (CalendarUtils.weekStart == Calendar.SATURDAY ? R.array.calendar_week_saturday : R.array.calendar_week_sunday));
        array.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mWeekTextColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mWeekSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (heightMode == MeasureSpec.AT_MOST) {
//            heightSize = mDisplayMetrics.densityDpi * 30;
//        }
//        if (widthMode == MeasureSpec.AT_MOST) {
//            widthSize = mDisplayMetrics.densityDpi * 300;
//        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int columnWidth = width / 7;
        for (int i = 0; i < mWeekString.length; i++) {
            String text = mWeekString[i];
            int fontWidth = (int) mPaint.measureText(text);
            int startX = columnWidth * i + (columnWidth - fontWidth) / 2;
            int startY = (int) (height / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            canvas.drawText(text, startX, startY, mPaint);
        }
    }

}

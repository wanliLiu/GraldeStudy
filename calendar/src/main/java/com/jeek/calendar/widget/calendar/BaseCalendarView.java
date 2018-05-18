package com.jeek.calendar.widget.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jeek.calendar.library.R;

import java.util.List;

/**
 * @author Soli
 * @Time 18-5-9 下午1:51
 */
public abstract class BaseCalendarView extends View {

    protected static final int NUM_COLUMNS = 7;
    protected static final int NUM_ROWS = 6;

    protected Paint mPaint;
    protected Paint mLunarPaint;

    protected int[] mHolidays;

    //只有月视图用了这个
    protected int mLastOrNextMonthTextColor;

    protected int mNormalDayColor;
    protected int mSelectDayColor;
    protected int mSelectBGColor;
    protected int mSelectBGTodayColor;
    protected int mCurrentDayColor;
    protected int mHintCircleColor;
    protected int mLunarTextColor;
    protected int mHolidayTextColor;
    protected int mCurrYear, mCurrMonth, mCurrDay;
    protected int mSelYear, mSelMonth, mSelDay;
    protected int mColumnSize, mRowSize, mSelectCircleSize;
    protected int defaultRowHeight = 0;

    protected int mDaySize;
    protected int mLunarTextSize;

    protected int mCircleRadius = 6;

    protected boolean mIsShowLunar;
    protected boolean mIsShowHint;
    protected boolean mIsShowHolidayHint;

    private GestureDetector mGestureDetector;

    protected Bitmap mRestBitmap, mWorkBitmap;

    public BaseCalendarView(Context context) {
        this(context, null);
    }

    public BaseCalendarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.obtainStyledAttributes(attrs, R.styleable.CalendarView));
    }

    /**
     *
     */
    protected void init(TypedArray array) {
        initAttrs(array);
        initGestureDetector();
        mRestBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_rest_day);
        mWorkBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_work_day);

        initPaint();
    }


    /**
     *
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        mPaint.setTextSize(mDaySize);

        mLunarPaint = new Paint();
        mLunarPaint.setAntiAlias(true);
        mLunarPaint.setTextSize(mLunarTextSize);
        mLunarPaint.setColor(mLunarTextColor);
    }

    /**
     * @param array
     */
    protected void initAttrs(TypedArray array) {
        Resources resources = getContext().getResources();
        if (array != null) {
            mSelectDayColor = array.getColor(R.styleable.CalendarView_selected_text_color, Color.parseColor("#FFFFFF"));
            mSelectBGColor = array.getColor(R.styleable.CalendarView_selected_circle_color, Color.parseColor("#E8E8E8"));
            mSelectBGTodayColor = array.getColor(R.styleable.CalendarView_selected_circle_today_color, Color.parseColor("#FF8594"));
            mNormalDayColor = array.getColor(R.styleable.CalendarView_normal_text_color, Color.parseColor("#575471"));
            mCurrentDayColor = array.getColor(R.styleable.CalendarView_today_text_color, Color.parseColor("#FF8594"));
            mHintCircleColor = array.getColor(R.styleable.CalendarView_hint_circle_color, Color.parseColor("#FE8595"));
            mLastOrNextMonthTextColor = array.getColor(R.styleable.CalendarView_last_or_next_text_color, Color.parseColor("#ACA9BC"));
            mLunarTextColor = array.getColor(R.styleable.CalendarView_lunar_text_color, Color.parseColor("#ACA9BC"));
            mHolidayTextColor = array.getColor(R.styleable.CalendarView_holiday_color, Color.parseColor("#A68BFF"));
            mDaySize = array.getDimensionPixelSize(R.styleable.CalendarView_day_text_size, resources.getDimensionPixelSize(R.dimen.size_day_text_siez));
            mLunarTextSize = array.getDimensionPixelSize(R.styleable.CalendarView_day_lunar_text_size, resources.getDimensionPixelSize(R.dimen.size_day_lunar_text_siez));
            mIsShowHint = array.getBoolean(R.styleable.CalendarView_show_task_hint, true);
            // TODO: 18-5-8  农历显示 切换有问题，目前先不管，后面再看，这里默认就不显示农历
            mIsShowLunar = array.getBoolean(R.styleable.CalendarView_show_lunar, false);
            // TODO: 18-5-8 节假日默认也不显示
            mIsShowHolidayHint = array.getBoolean(R.styleable.CalendarView_show_holiday_hint, false);
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF");
            mSelectBGColor = Color.parseColor("#E8E8E8");
            mSelectBGTodayColor = Color.parseColor("#FF8594");
            mNormalDayColor = Color.parseColor("#575471");
            mCurrentDayColor = Color.parseColor("#FF8594");
            mHintCircleColor = Color.parseColor("#FE8595");
            mLastOrNextMonthTextColor = Color.parseColor("#ACA9BC");
            mHolidayTextColor = Color.parseColor("#A68BFF");
            mDaySize = resources.getDimensionPixelSize(R.dimen.size_day_text_siez);
            mLunarTextSize = resources.getDimensionPixelSize(R.dimen.size_day_lunar_text_siez);
            mIsShowHint = true;
            mIsShowLunar = true;
            mIsShowHolidayHint = true;
        }

        defaultRowHeight = resources.getDimensionPixelSize(R.dimen.calendar_row_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(widthSize, getMaxRowNum() * defaultRowHeight);
        initSize();
    }

    /**
     *
     */
    private void initSize() {
        mColumnSize = getMeasuredWidth() / NUM_COLUMNS;
        mRowSize = getMeasuredHeight() / getActualRowNum();
        mSelectCircleSize = (int) (mColumnSize / 3.2);
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.3);
        }
    }

    /**
     * @return
     */
    protected abstract int getMaxRowNum();

    /**
     * @return
     */
    protected abstract int getActualRowNum();

    /**
     *
     */
    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                doClickAction((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 点击
     *
     * @param x
     * @param y
     */
    protected void doClickAction(int x, int y) {

    }

    /**
     * @param year
     * @param month
     * @param day
     */
    public void setSelectYearMonth(int year, int month, int day) {
        mSelYear = year;
        mSelMonth = month;
        mSelDay = day;
    }

    /**
     * 获取当前选择年
     *
     * @return
     */
    public int getSelectYear() {
        return mSelYear;
    }

    /**
     * 获取当前选择月
     *
     * @return
     */
    public int getSelectMonth() {
        return mSelMonth;
    }

    /**
     * 获取当前选择日
     *
     * @return
     */
    public int getSelectDay() {
        return this.mSelDay;
    }


    /**
     * 添加多个圆点提示
     *
     * @param hints
     */
    public void addTaskHints(List<Integer> hints) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(getContext()).addTaskHints(mSelYear, mSelMonth, hints);
            invalidate();
        }
    }

    /**
     * 删除多个圆点提示
     *
     * @param hints
     */
    public void removeTaskHints(List<Integer> hints) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(getContext()).removeTaskHints(mSelYear, mSelMonth, hints);
            invalidate();
        }
    }

    /**
     * 添加一个圆点提示
     *
     * @param day
     */
    public boolean addTaskHint(Integer day) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(getContext()).addTaskHint(mSelYear, mSelMonth, day)) {
                invalidate();
                return true;
            }
        }
        return false;
    }

    /**
     * 删除一个圆点提示
     *
     * @param day
     */
    public boolean removeTaskHint(Integer day) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(getContext()).removeTaskHint(mSelYear, mSelMonth, day)) {
                invalidate();
                return true;
            }
        }
        return false;
    }

}

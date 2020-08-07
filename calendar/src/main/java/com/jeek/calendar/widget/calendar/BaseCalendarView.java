package com.jeek.calendar.widget.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.jeek.calendar.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Soli
 * @Time 18-5-9 下午1:51
 */
public abstract class BaseCalendarView extends View implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final boolean isDebug = false;

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

    //-------------------------------------------------------------------------event--------------------------------------------------------------------------------
    private final int LOADER_THROTTLE_DELAY = 10;
    private final String WHERE_CALENDARS_VISIBLE = Calendars.VISIBLE + "=1";
    private final String INSTANCES_SORT_ORDER = Instances.START_DAY + "," + Instances.START_MINUTE + "," + Instances.TITLE;

    protected ArrayList<ArrayList<Event>> mEventDayList = new ArrayList<>();

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
        Calendar calendar = Calendar.getInstance();
        setSelectYearMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

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


    /**
     * @param msg
     */
    protected void log(String msg) {
        if (isDebug) {
            Log.e("CalendarView", msg);
        }
    }

    //-------------------------------------------------------------------------event--------------------------------------------------------------------------------

    /**
     * 初始化 然后加载数据
     *
     * @param position
     */
    public void setPosition(int position) {
        initTaskLoader(position);
    }

    /**
     * @return
     */
    private LoaderManager getLoadManager() {
        return ((FragmentActivity) getContext()).getSupportLoaderManager();
    }

    /**
     *
     */
    private void initTaskLoader(int position) {
        getLoadManager().initLoader(position, null, this);
        LoaderManager.enableDebugLogging(isDebug);
    }

    /**
     * 之类重写获取相应的范围
     *
     * @return
     */
    private Uri getQeryRange() {
        return updateUri(mSelYear, mSelMonth, 1, mSelYear, mSelMonth, CalendarUtils.getMonthDays(mSelYear, mSelMonth));
    }

    /**
     * 获取要查询的范围
     *
     * @param startYear
     * @param startMonh
     * @param startDay
     * @param endYear
     * @param endMonth
     * @param endDay
     * @return
     */
    protected Uri updateUri(int startYear, int startMonh, int startDay, int endYear, int endMonth, int endDay) {
        Calendar temp = Calendar.getInstance();
        long start, end;
        temp.set(startYear, startMonh, startDay);
        temp.set(Calendar.HOUR_OF_DAY, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);
        start = temp.getTimeInMillis();

        temp.set(endYear, endMonth, endDay);
        temp.set(Calendar.HOUR_OF_DAY, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);
        end = temp.getTimeInMillis();

        // Create a new uri with the updated times
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        log("开始加载：" + sdf.format(new Date(start)) + "------" + sdf.format(new Date(end)) + "区间事件");
        return builder.build();
    }

    protected String updateWhere() {
        // TODO fix selection/selection args after b/3206641 is fixed
        String where = WHERE_CALENDARS_VISIBLE;
//        if (mHideDeclined || !mShowDetailsInMonth) {
//            where += " AND " + Instances.SELF_ATTENDEE_STATUS + "!=" + Attendees.ATTENDEE_STATUS_DECLINED;
//        }
        return where;
    }


    /**
     * @param uri
     * @return
     */
    private int[] updateLoadedDays(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        int size = pathSegments.size();
        if (size <= 2) {
            return new int[2];
        }
        long first = Long.parseLong(pathSegments.get(size - 2));
        long last = Long.parseLong(pathSegments.get(size - 1));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        log("结束加载：" + sdf.format(new Date(first)) + "------" + sdf.format(new Date(last)) + "区间事件");

        int[] julian = new int[2];
        Time time = new Time();
        time.set(first);
        julian[0] = Time.getJulianDay(first, time.gmtoff);

        time.set(last);
        julian[1] = Time.getJulianDay(last, time.gmtoff);
        return julian;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(), getQeryRange(), Event.EVENT_PROJECTION, updateWhere(), null /* WHERE_CALENDARS_SELECTED_ARGS */, INSTANCES_SORT_ORDER);
//        loader.setUpdateThrottle(LOADER_THROTTLE_DELAY);
        loader.startLoading();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mEventDayList.clear();

        if (data != null && data.getCount() > 0) {
            Uri uri = ((CursorLoader) loader).getUri();
            int[] julian = updateLoadedDays(uri);
            log("加载完成，总共有事件：" + data.getCount());
            ArrayList<Event> events = new ArrayList<>();
            Event.buildEventsFromCursor(events, data, getContext(), julian[0], julian[1]);
            if (events.size() > 0)
                setEvents(julian[0], julian[1] - julian[0] + 1, events);
        }

        //update ui
        invalidate();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * @param firstJulianDay
     * @param numDays
     * @param events
     */
    protected void setEvents(int firstJulianDay, int numDays, ArrayList<Event> events) {
        // Create a new list, this is necessary since the weeks are referencing
        // pieces of the old list
        ArrayList<ArrayList<Event>> eventDayList = new ArrayList<>();
        for (int i = 0; i < numDays; i++) {
            eventDayList.add(new ArrayList<Event>());
        }
        // Compute the new set of days with events
        for (Event event : events) {
            int startDay = event.startDay - firstJulianDay;
            int endDay = event.endDay - firstJulianDay + 1;
            if (startDay < numDays || endDay >= 0) {
                if (startDay < 0) {
                    startDay = 0;
                }
                if (startDay > numDays) {
                    continue;
                }
                if (endDay < 0) {
                    continue;
                }
                if (endDay > numDays) {
                    endDay = numDays;
                }
                for (int j = startDay; j < endDay; j++) {
                    eventDayList.get(j).add(event);
                }
            }
        }
        log("处理完成,对应到天的事件有：" + eventDayList.size());
        mEventDayList = eventDayList;
    }

    /**
     * @param day monthview 0--day
     *            weekview 0---6
     * @return
     */
    protected ArrayList<Event> getDayEvents(int day) {
        if (mEventDayList != null && mEventDayList.size() > 0 && day < mEventDayList.size()) {
            return mEventDayList.get(day);
        }
        return null;
    }

    /**
     * @return
     */
    public ArrayList<Event> getDayEvents() {
        return getDayEvents(getSelectDay());
    }
}

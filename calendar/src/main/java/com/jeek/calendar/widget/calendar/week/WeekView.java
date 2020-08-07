package com.jeek.calendar.widget.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.jeek.calendar.widget.calendar.BaseCalendarView;
import com.jeek.calendar.widget.calendar.CalendarUtils;
import com.jeek.calendar.widget.calendar.Event;
import com.jeek.calendar.widget.calendar.LunarCalendarUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class WeekView extends BaseCalendarView {

    private int[] mHolidays;
    private String mHolidayOrLunarText[];
    private DateTime mStartDate;
    private OnWeekClickListener mOnWeekClickListener;

    public WeekView(Context context) {
        super(context);
    }

    public WeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void init(TypedArray array) {
        super.init(array);
        init(array, new DateTime());
    }

    /**
     * @param array
     * @param dateTime
     */
    public void init(TypedArray array, DateTime dateTime) {
        initAttrs(array);

        mStartDate = dateTime;
        int holidays[] = CalendarUtils.getInstance(getContext()).getHolidays(mStartDate.getYear(), mStartDate.getMonthOfYear());
        int row = CalendarUtils.getWeekRow(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
        mHolidays = new int[7];
        System.arraycopy(holidays, row * 7, mHolidays, 0, mHolidays.length);

        initWeek();
    }

    /**
     *
     */
    private void initWeek() {
        Calendar calendar = Calendar.getInstance();
        mCurrYear = calendar.get(Calendar.YEAR);
        mCurrMonth = calendar.get(Calendar.MONTH);
        mCurrDay = calendar.get(Calendar.DATE);
        DateTime endDate = mStartDate.plusDays(7);
        if (mStartDate.getMillis() <= System.currentTimeMillis() && endDate.getMillis() > System.currentTimeMillis()) {
            if (mStartDate.getMonthOfYear() != endDate.getMonthOfYear()) {
                if (mCurrDay < mStartDate.getDayOfMonth()) {
                    setSelectYearMonth(mStartDate.getYear(), endDate.getMonthOfYear() - 1, mCurrDay);
                } else {
                    setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mCurrDay);
                }
            } else {
                setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mCurrDay);
            }
        } else {
            setSelectYearMonth(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
        }
    }


    @Override
    protected Uri updateUri(int startYear, int startMonh, int startDay, int endYear, int endMonth, int endDay) {
        DateTime endDate = mStartDate.plusDays(6);
        return super.updateUri(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth(),
                endDate.getYear(), endDate.getMonthOfYear() - 1, endDate.getDayOfMonth());
    }

    @Override
    protected int getMaxRowNum() {
        return 1;
    }

    @Override
    protected int getActualRowNum() {
        return 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clearData();
        int selected = drawThisWeek(canvas);
        drawLunarText(canvas, selected);
        drawHintCircle(canvas);
        drawHoliday(canvas);
    }

    private void clearData() {
        mHolidayOrLunarText = new String[NUM_COLUMNS];
    }

    /**
     * @param canvas
     * @return
     */
    private int drawThisWeek(Canvas canvas) {
        int selected = 0;
        for (int i = 0; i < NUM_COLUMNS; i++) {
            DateTime date = mStartDate.plusDays(i);
            int day = date.getDayOfMonth();
            String dayString = String.valueOf(day);
            int startX = (int) (mColumnSize * i + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            if (day == mSelDay) {
                int startRecX = mColumnSize * i;
                int endRecX = startRecX + mColumnSize;
                if (date.getYear() == mCurrYear && date.getMonthOfYear() - 1 == mCurrMonth && day == mCurrDay) {
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    mPaint.setColor(mSelectBGColor);
                }
                canvas.drawCircle((startRecX + endRecX) / 2, mRowSize / 2, mSelectCircleSize, mPaint);
            }
            if (day == mSelDay) {
                selected = i;
                mPaint.setColor(mSelectDayColor);
            } else if (date.getYear() == mCurrYear && date.getMonthOfYear() - 1 == mCurrMonth && day == mCurrDay && mCurrYear == mSelYear) {//&& day != mSelDay
                dayString = "今";
                mPaint.setColor(mCurrentDayColor);
                startX = (int) (mColumnSize * i + (mColumnSize - mPaint.measureText(dayString)) / 2);
                startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            mHolidayOrLunarText[i] = CalendarUtils.getHolidayFromSolar(date.getYear(), date.getMonthOfYear() - 1, day);
        }
        return selected;
    }

    /**
     * 绘制农历
     *
     * @param canvas
     * @param selected
     */
    private void drawLunarText(Canvas canvas, int selected) {
        if (mIsShowLunar) {
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(mStartDate.getYear(), mStartDate.getMonthOfYear(), mStartDate.getDayOfMonth()));
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            int days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            int day = lunar.lunarDay;
            for (int i = 0; i < NUM_COLUMNS; i++) {
                if (day > days) {
                    day = 1;
                    boolean isAdd = true;
                    if (lunar.lunarMonth == 12) {
                        lunar.lunarMonth = 1;
                        lunar.lunarYear = lunar.lunarYear + 1;
                        isAdd = false;
                    }
                    if (lunar.lunarMonth == leapMonth) {
                        days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
                    } else {
                        if (isAdd) {
                            lunar.lunarMonth++;
                            days = LunarCalendarUtils.daysInLunarMonth(lunar.lunarYear, lunar.lunarMonth);
                        }
                    }
                }
                mLunarPaint.setColor(mHolidayTextColor);
                String dayString = mHolidayOrLunarText[i];
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                }
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarDayString(day);
                    mLunarPaint.setColor(mLunarTextColor);
                }
                if ("初一".equals(dayString)) {
                    DateTime curDay = mStartDate.plusDays(i);
                    LunarCalendarUtils.Lunar chuyi = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(curDay.getYear(), curDay.getMonthOfYear(), curDay.getDayOfMonth()));
                    dayString = LunarCalendarUtils.getLunarFirstDayString(chuyi.lunarMonth, chuyi.isLeap);
                }
                if (i == selected) {
                    mLunarPaint.setColor(mSelectDayColor);
                }
                int startX = (int) (mColumnSize * i + (mColumnSize - mLunarPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * 0.72 - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2);
                canvas.drawText(dayString, startX, startY, mLunarPaint);
                day++;
            }
        }
    }

    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(), mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % NUM_COLUMNS;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth() - distance, distance, mColumnSize * (column + 1) - distance, mRestBitmap.getHeight() + distance);
                if (mHolidays[i] == 1) {
                    canvas.drawBitmap(mRestBitmap, rect, rectF, null);
                } else if (mHolidays[i] == 2) {
                    canvas.drawBitmap(mWorkBitmap, rect, rectF, null);
                }
            }
        }
    }

    /**
     * 绘制圆点提示
     *
     * @param canvas
     */
    private void drawHintCircle(Canvas canvas) {
        if (mIsShowHint) {
            if (mEventDayList != null && mEventDayList.size() > 0 && mEventDayList.size() == NUM_COLUMNS) {
                mPaint.setColor(mHintCircleColor);
                for (int i = 0; i < NUM_COLUMNS; i++) {
                    ArrayList<Event> list = mEventDayList.get(i);
                    if (list == null || list.size() == 0)
                        continue;
                    drawHintCircle(i, canvas);
                }
            }
//            mPaint.setColor(mHintCircleColor);
//            int startMonth = mStartDate.getMonthOfYear();
//            int endMonth = mStartDate.plusDays(NUM_COLUMNS).getMonthOfYear();
//            int startDay = mStartDate.getDayOfMonth();
//            if (startMonth == endMonth) {
//                List<Integer> hints = CalendarUtils.getInstance(getContext()).getTaskHints(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1);
//                for (int i = 0; i < NUM_COLUMNS; i++) {
//                    drawHintCircle(hints, startDay + i, i, canvas);
//                }
//            } else {
//                for (int i = 0; i < NUM_COLUMNS; i++) {
//                    List<Integer> hints = CalendarUtils.getInstance(getContext()).getTaskHints(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1);
//                    List<Integer> nextHints = CalendarUtils.getInstance(getContext()).getTaskHints(mStartDate.getYear(), mStartDate.getMonthOfYear());
//                    DateTime date = mStartDate.plusDays(i);
//                    int month = date.getMonthOfYear();
//                    if (month == startMonth) {
//                        drawHintCircle(hints, date.getDayOfMonth(), i, canvas);
//                    } else {
//                        drawHintCircle(nextHints, date.getDayOfMonth(), i, canvas);
//                    }
//                }
//            }
        }
    }

    /**
     * @param col
     * @param canvas
     */
    private void drawHintCircle(int col, Canvas canvas) {
        float circleX = (float) (mColumnSize * col + mColumnSize * 0.5);
        float circleY = (float) (mRowSize * 0.75);
        canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
    }

    /**
     * @param hints
     * @param day
     * @param col
     * @param canvas
     */
    private void drawHintCircle(List<Integer> hints, int day, int col, Canvas canvas) {
        if (!hints.contains(day)) return;
        float circleX = (float) (mColumnSize * col + mColumnSize * 0.5);
        float circleY = (float) (mRowSize * 0.75);
        canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
    }

    /**
     * @param x
     * @param y
     */
    @Override
    protected void doClickAction(int x, int y) {
        if (y > getHeight())
            return;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        DateTime date = mStartDate.plusDays(column);
        clickThisWeek(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
    }

    /**
     * @param year
     * @param month
     * @param day
     */
    public void clickThisWeek(int year, int month, int day) {
        if (mOnWeekClickListener != null) {
            mOnWeekClickListener.onClickDate(year, month, day, getDayEvents(day));
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }

    public void setOnWeekClickListener(OnWeekClickListener onWeekClickListener) {
        mOnWeekClickListener = onWeekClickListener;
    }

    public DateTime getStartDate() {
        return mStartDate;
    }

    public DateTime getEndDate() {
        return mStartDate.plusDays(6);
    }

    @Override
    protected ArrayList<Event> getDayEvents(int day) {
        if (mStartDate == null)
            return null;

        int index = 0;
        for (; index < NUM_COLUMNS; index++) {
            DateTime time = mStartDate.plusDays(index);
            if (time.getDayOfMonth() == day)
                break;
        }

        if (index == NUM_COLUMNS)
            return null;

        return super.getDayEvents(index);
    }

    @Override
    protected void setEvents(int firstJulianDay, int numDays, ArrayList<Event> events) {
        super.setEvents(firstJulianDay, numDays, events);
        clickThisWeek(getSelectYear(), getSelectMonth(), getSelectDay());
    }
}

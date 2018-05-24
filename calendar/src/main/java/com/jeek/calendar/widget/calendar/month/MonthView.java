package com.jeek.calendar.widget.calendar.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.jeek.calendar.widget.calendar.BaseCalendarView;
import com.jeek.calendar.widget.calendar.CalendarUtils;
import com.jeek.calendar.widget.calendar.Event;
import com.jeek.calendar.widget.calendar.LunarCalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public class MonthView extends BaseCalendarView {

    private boolean isNeedPreNext = true, isShowSelect = true;

    private int mWeekRow;
    //一个月视图的总共天数  todo 这个到时候根据实际的月row数量来动态计算
    private int totalMonthDays = 42;
    private int[][] mDaysText;
    private String[][] mHolidayOrLunarText;

    /**
     * 月份实际有多少行数
     */
    private int actRows = 0;

    private OnMonthClickListener mDateClickListener;

    public MonthView(Context context) {
        super(context);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MonthView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void init(TypedArray array) {
        super.init(array);
        Calendar calendar = Calendar.getInstance();
        init(array, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    /**
     * 是否显示last or past
     *
     * @param needPreNext
     */
    public void setNeedPreNext(boolean needPreNext) {
        isNeedPreNext = needPreNext;
    }

    /**
     * 是否有选中的效果
     *
     * @param showSelect
     */
    public void setShowSelect(boolean showSelect) {
        isShowSelect = showSelect;
    }

    /**
     * @param array
     * @param year
     * @param month
     */
    public void init(TypedArray array, int year, int month) {
        initAttrs(array);

        mSelYear = year;
        mSelMonth = month;

        mHolidays = CalendarUtils.getInstance(getContext()).getHolidays(mSelYear, mSelMonth + 1);

        initMonth();
    }


    /**
     *
     */
    private void initMonth() {
        Calendar calendar = Calendar.getInstance();
        mCurrYear = calendar.get(Calendar.YEAR);
        mCurrMonth = calendar.get(Calendar.MONTH);
        mCurrDay = calendar.get(Calendar.DATE);
        if (mSelYear == mCurrYear && mSelMonth == mCurrMonth) {
            setSelectYearMonth(mSelYear, mSelMonth, mCurrDay);
        } else {
            setSelectYearMonth(mSelYear, mSelMonth, 1);
        }
    }

    @Override
    public void setSelectYearMonth(int year, int month, int day) {
        super.setSelectYearMonth(year, month, day);
        actRows = CalendarUtils.getMonthRows(mSelYear, mSelMonth);
        totalMonthDays = actRows * NUM_COLUMNS;
        requestLayout();
    }

    @Override
    protected int getMaxRowNum() {
        return NUM_ROWS;
    }

    @Override
    protected int getActualRowNum() {
        return actRows;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clearData();
        drawLastMonth(canvas);
        int selected[] = drawThisMonth(canvas);
        drawNextMonth(canvas);
        drawHintCircle(canvas);
        drawLunarText(canvas, selected);
        drawHoliday(canvas);
    }

    /**
     *
     */
    private void clearData() {
        mDaysText = new int[actRows][NUM_COLUMNS];
        mHolidayOrLunarText = new String[actRows][NUM_COLUMNS];
    }

    /**
     * 绘制上个月的结合日期
     *
     * @param canvas
     */
    private void drawLastMonth(Canvas canvas) {
        if (!isNeedPreNext)
            return;
        int lastYear, lastMonth;
        if (mSelMonth == 0) {
            lastYear = mSelYear - 1;
            lastMonth = 11;
        } else {
            lastYear = mSelYear;
            lastMonth = mSelMonth - 1;
        }
        mPaint.setColor(mLastOrNextMonthTextColor);
        int monthDays = CalendarUtils.getMonthDays(lastYear, lastMonth);
        int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));

        for (int day = 0; day < dayoffset; day++) {
            mDaysText[0][day] = monthDays - dayoffset + day + 1;
            String dayString = String.valueOf(mDaysText[0][day]);
            int startX = (int) (mColumnSize * day + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            canvas.drawText(dayString, startX, startY, mPaint);
            mHolidayOrLunarText[0][day] = CalendarUtils.getHolidayFromSolar(lastYear, lastMonth, mDaysText[0][day]);
        }
    }

    /**
     * 绘制这个月的日期
     *
     * @param canvas
     * @return
     */
    private int[] drawThisMonth(Canvas canvas) {
        String dayString;
        int selectedPoint[] = new int[2];
        int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
        int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));

        for (int day = 0; day < monthDays; day++) {
            int col = (day + dayoffset) % NUM_COLUMNS;
            int row = (day + dayoffset) / NUM_COLUMNS;

            mDaysText[row][col] = day + 1;
            dayString = String.valueOf(mDaysText[row][col]);

            int startX = (int) (mColumnSize * col + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            if (isShowSelect && dayString.equals(String.valueOf(mSelDay))) {
                int startRecX = mColumnSize * col;
                int startRecY = mRowSize * row;
                int endRecX = startRecX + mColumnSize;
                int endRecY = startRecY + mRowSize;
                if (mSelYear == mCurrYear && mCurrMonth == mSelMonth && day + 1 == mCurrDay) {
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    mPaint.setColor(mSelectBGColor);
                }
                canvas.drawCircle((startRecX + endRecX) / 2, (startRecY + endRecY) / 2, mSelectCircleSize, mPaint);
                mWeekRow = row + 1;
            }
            if (isShowSelect && dayString.equals(String.valueOf(mSelDay))) {
                selectedPoint[0] = row;
                selectedPoint[1] = col;
                mPaint.setColor(mSelectDayColor);
            } else if (dayString.equals(String.valueOf(mCurrDay)) && mCurrMonth == mSelMonth && mCurrYear == mSelYear) {// && mCurrDay != mSelDay
                dayString = "今";
                mPaint.setColor(mCurrentDayColor);
                startX = (int) (mColumnSize * col + (mColumnSize - mPaint.measureText(dayString)) / 2);
                startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            mHolidayOrLunarText[row][col] = CalendarUtils.getHolidayFromSolar(mSelYear, mSelMonth, mDaysText[row][col]);
        }
        return selectedPoint;
    }

    /**
     * 和这个月结合的下一月数据
     *
     * @param canvas
     */
    private void drawNextMonth(Canvas canvas) {
        if (!isNeedPreNext)
            return;

        mPaint.setColor(mLastOrNextMonthTextColor);
        int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
        int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));
        int lastDayoffset = CalendarUtils.findDayOffset(CalendarUtils.getDayWeek(mSelYear, mSelMonth, monthDays));

        int nextMonthDays = totalMonthDays - (monthDays + dayoffset);
        int nextMonth = mSelMonth + 1;


        int nextYear = mSelYear;
        if (nextMonth == 12) {
            nextMonth = 0;
            nextYear += 1;
        }

        int row = actRows - 1;
        for (int day = 0; day < nextMonthDays; day++) {
            int column = lastDayoffset + day + 1;
            try {
                mDaysText[row][column] = day + 1;
                mHolidayOrLunarText[row][column] = CalendarUtils.getHolidayFromSolar(nextYear, nextMonth, mDaysText[row][column]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dayString = String.valueOf(mDaysText[row][column]);
            int startX = (int) (mColumnSize * column + (mColumnSize - mPaint.measureText(dayString)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint.ascent() + mPaint.descent()) / 2);
            canvas.drawText(dayString, startX, startY, mPaint);
        }
    }

    /**
     * 绘制农历
     *
     * @param canvas
     * @param selected
     */
    private void drawLunarText(Canvas canvas, int[] selected) {
        if (mIsShowLunar) {
            int firstYear, firstMonth, firstDay, monthDays;
            int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));
            int weekNumber = CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth);

            if (weekNumber == 1) {
                firstYear = mSelYear;
                firstMonth = mSelMonth + 1;
                firstDay = dayoffset + 1;
                monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth);
            } else {
                if (mSelMonth == 0) {
                    firstYear = mSelYear - 1;
                    firstMonth = 11;
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth);
                    firstMonth = 12;
                } else {
                    firstYear = mSelYear;
                    firstMonth = mSelMonth - 1;
                    monthDays = CalendarUtils.getMonthDays(firstYear, firstMonth);
                    firstMonth = mSelMonth;
                }
                firstDay = monthDays - dayoffset + 1;
            }
            LunarCalendarUtils.Lunar lunar = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(firstYear, firstMonth, firstDay));
            int days;
            int day = lunar.lunarDay;
            int leapMonth = LunarCalendarUtils.leapMonth(lunar.lunarYear);
            days = LunarCalendarUtils.daysInMonth(lunar.lunarYear, lunar.lunarMonth, lunar.isLeap);
            boolean isChangeMonth = false;
            for (int i = 0; i < totalMonthDays; i++) {
                int column = i % NUM_COLUMNS;
                int row = i / NUM_COLUMNS;
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
                if (firstDay > monthDays) {
                    firstDay = 1;
                    isChangeMonth = true;
                }
                if (row == 0 && mDaysText[row][column] >= 23 || row >= 4 && mDaysText[row][column] <= 14) {
                    mLunarPaint.setColor(mLunarTextColor);
                } else {
                    mLunarPaint.setColor(mHolidayTextColor);
                }
                String dayString = mHolidayOrLunarText[row][column];
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarHoliday(lunar.lunarYear, lunar.lunarMonth, day);
                }
                if ("".equals(dayString)) {
                    dayString = LunarCalendarUtils.getLunarDayString(day);
                    mLunarPaint.setColor(mLunarTextColor);
                }
                if ("初一".equals(dayString)) {
                    int curYear = firstYear, curMonth = firstMonth;
                    if (isChangeMonth) {
                        curMonth++;
                        if (curMonth == 13) {
                            curMonth = 1;
                            curYear++;
                        }
                    }
                    LunarCalendarUtils.Lunar chuyi = LunarCalendarUtils.solarToLunar(new LunarCalendarUtils.Solar(curYear, curMonth, firstDay));
                    dayString = LunarCalendarUtils.getLunarFirstDayString(chuyi.lunarMonth, chuyi.isLeap);
                }
                if (selected[0] == row && selected[1] == column) {
                    mLunarPaint.setColor(mSelectDayColor);
                }
                int startX = (int) (mColumnSize * column + (mColumnSize - mLunarPaint.measureText(dayString)) / 2);
                int startY = (int) (mRowSize * row + mRowSize * 0.72 - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2);
                canvas.drawText(dayString, startX, startY, mLunarPaint);
                day++;
                firstDay++;
            }
        }
    }

    /**
     * holiday这个，数据不行，不能够根据设置动态调，开始是星期日还是星期一
     *
     * @param canvas
     */
    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(), mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % NUM_COLUMNS;
                int row = i / NUM_COLUMNS;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth() - distance, mRowSize * row + distance, mColumnSize * (column + 1) - distance, mRowSize * row + mRestBitmap.getHeight() + distance);
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
            int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
            if (mEventDayList != null && mEventDayList.size() > 0 && mEventDayList.size() == monthDays) {
                mPaint.setColor(mHintCircleColor);
                int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));
                for (int day = 0; day < monthDays; day++) {
                    ArrayList<Event> list = mEventDayList.get(day);
                    if (list == null || list.size() == 0)
                        continue;
                    int col = (day + dayoffset) % NUM_COLUMNS;
                    int row = (day + dayoffset) / NUM_COLUMNS;
                    float circleX = (float) (mColumnSize * col + mColumnSize * 0.5);
                    float circleY = (float) (mRowSize * row + mRowSize * 0.75);
                    canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
                }
            }
//            List<Integer> hints = CalendarUtils.getInstance(getContext()).getTaskHints(mSelYear, mSelMonth);
//            if (hints.size() > 0) {
//                mPaint.setColor(mHintCircleColor);
//                int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
//                int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));
//                for (int day = 0; day < monthDays; day++) {
//                    if (!hints.contains(day + 1)) continue;
//                    int col = (day + dayoffset) % NUM_COLUMNS;
//                    int row = (day + dayoffset) / NUM_COLUMNS;
//                    float circleX = (float) (mColumnSize * col + mColumnSize * 0.5);
//                    float circleY = (float) (mRowSize * row + mRowSize * 0.75);
//                    canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
//                }
//            }
        }
    }

    /**
     * @param x
     * @param y
     */
    @Override
    protected void doClickAction(int x, int y) {
        if (y > getHeight())
            return;
        int row = y / mRowSize;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        int clickYear = mSelYear, clickMonth = mSelMonth;
        //第一行和最后一行比较特殊
        if (row == 0) {
            if (mDaysText[row][column] >= 23) {//第一行,以最小的28天来算,点击只要大于或等于23就认为是点击上一个月的时间
                if (mSelMonth == 0) {
                    clickYear = mSelYear - 1;
                    clickMonth = 11;
                } else {
                    clickYear = mSelYear;
                    clickMonth = mSelMonth - 1;
                }
                if (isNeedPreNext && mDateClickListener != null) {
                    mDateClickListener.onClickLastMonth(clickYear, clickMonth, mDaysText[row][column],null);
                }
            } else {
                if (mDaysText[row][column] > 0)
                    clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        } else {
            int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
            int dayoffset = CalendarUtils.findDayOffset(CalendarUtils.getFirstDayWeek(mSelYear, mSelMonth));
            int nextMonthDays = totalMonthDays - monthDays - dayoffset;
            if (mDaysText[row][column] <= nextMonthDays && row >= 4) {
                //点击了这个月的显示的下个月的数据
                if (mSelMonth == 11) {
                    clickYear = mSelYear + 1;
                    clickMonth = 0;
                } else {
                    clickYear = mSelYear;
                    clickMonth = mSelMonth + 1;
                }
                if (isNeedPreNext && mDateClickListener != null) {
                    mDateClickListener.onClickNextMonth(clickYear, clickMonth, mDaysText[row][column],null);
                }
            } else {
                if (mDaysText[row][column] > 0)
                    clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        }
    }

    /**
     * 跳转到某日期
     *
     * @param year
     * @param month
     * @param day
     */
    public void clickThisMonth(int year, int month, int day) {
        if (mDateClickListener != null) {
            mDateClickListener.onClickThisMonth(year, month, day, getDayEvents(day - 1));
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }


    public int getRowSize() {
        return mRowSize;
    }

    /**
     * 这个月有多少行
     *
     * @return
     */
    public int getRows() {
        return CalendarUtils.getMonthRows(mSelYear, mSelMonth);
    }

    /**
     * 获取选中的一个月的哪一行
     *
     * @return
     */
    public int getWeekRow() {
        return mWeekRow;
    }

    /**
     * 设置点击日期监听
     *
     * @param dateClickListener
     */
    public void setOnDateClickListener(OnMonthClickListener dateClickListener) {
        this.mDateClickListener = dateClickListener;
    }
}


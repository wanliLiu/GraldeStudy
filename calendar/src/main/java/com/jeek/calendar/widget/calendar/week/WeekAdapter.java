package com.jeek.calendar.widget.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.jeek.calendar.library.R;
import com.jeek.calendar.widget.calendar.CalendarUtils;

import org.joda.time.DateTime;

import java.util.Calendar;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public class WeekAdapter extends PagerAdapter {

    private SparseArray<WeekView> mViews;
    private Context mContext;
    private TypedArray mArray;
    private WeekCalendarView mWeekCalendarView;
    private DateTime mStartDate;
    private DateTime firstDate;
    private int mWeekCount = CalendarUtils.defaultWeekCount;

    public WeekAdapter(Context context, TypedArray array, WeekCalendarView weekCalendarView) {
        mWeekCount = array.getInteger(R.styleable.CalendarView_count, CalendarUtils.defaultWeekCount);
        mContext = context;
        mArray = array;
        mWeekCalendarView = weekCalendarView;
        mViews = new SparseArray<>();
        initStartDate();
    }

    private void initStartDate() {
        mStartDate = new DateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(mStartDate.getYear(), mStartDate.getMonthOfYear() - 1, mStartDate.getDayOfMonth());
        mStartDate = mStartDate.plusDays(-CalendarUtils.findDayOffset(calendar.get(Calendar.DAY_OF_WEEK)));

        firstDate = mStartDate.plusWeeks(-mWeekCount / 2);
    }

    @Override
    public int getCount() {
        return mWeekCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //随时保持有三个
        for (int i = 0; i < 3; i++) {
            int temp = position - 2 + i;
            if (temp >= 0 && temp < mWeekCount && mViews.get(temp) == null) {
                instanceWeekView(temp);
            }
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public SparseArray<WeekView> getViews() {
        return mViews;
    }

    public int getWeekCount() {
        return mWeekCount;
    }

    /**
     * @param position
     * @return
     */
    public WeekView instanceWeekView(int position) {
        WeekView weekView = new WeekView(mContext);
        weekView.init(mArray, mStartDate.plusWeeks(position - mWeekCount / 2));
        weekView.setId(position);
        weekView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        weekView.setOnWeekClickListener(mWeekCalendarView);
        weekView.invalidate();
        mViews.put(position, weekView);
        return weekView;
    }

    public DateTime getFirstDate() {
        return firstDate;
    }
}

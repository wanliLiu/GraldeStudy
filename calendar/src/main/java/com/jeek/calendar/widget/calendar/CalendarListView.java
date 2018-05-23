package com.jeek.calendar.widget.calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jeek.calendar.library.R;
import com.jeek.calendar.widget.calendar.month.MonthView;
import com.jeek.calendar.widget.calendar.month.OnMonthClickListener;

import org.joda.time.DateTime;

/**
 * @author Soli
 * @Time 18-5-7 下午5:17
 */
public class CalendarListView extends RecyclerView implements OnMonthClickListener {

    private OnCalendarClickListener mOnCalendarClickListener;

    public CalendarListView(Context context) {
        this(context, null);
    }

    public CalendarListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initMonthAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.CalendarView));
    }

    private void initMonthAdapter(Context context, TypedArray array) {
        LinearLayoutManager manager = new LinearLayoutManager(context);
        setLayoutManager(manager);
        calendarListAdapter mMonthAdapter = new calendarListAdapter(array, this);
        setAdapter(mMonthAdapter);
        manager.scrollToPositionWithOffset(mMonthAdapter.getItemCount() / 2,60);
    }

    @Override
    public void onClickThisMonth(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    @Override
    public void onClickLastMonth(int year, int month, int day) {
    }

    @Override
    public void onClickNextMonth(int year, int month, int day) {

    }

    private class calendarListAdapter extends Adapter<ViewHolder> {

        private TypedArray mArray;
        private CalendarListView mMonthCalendarView;
        private int mMonthCount;
        private int todayYear, todayMonth;

        public calendarListAdapter(TypedArray array, CalendarListView monthCalendarView) {
            mArray = array;
            mMonthCalendarView = monthCalendarView;
            mMonthCount = array.getInteger(R.styleable.CalendarView_count, CalendarUtils.defaultMonthCount);

            DateTime time = new DateTime();
            todayYear = time.getYear();
            todayMonth = time.getMonthOfYear() - 1;
        }

        private int[] getYearAndMonth(int position) {
            int date[] = new int[2];
            DateTime time = new DateTime();
            time = time.plusMonths(position - mMonthCount / 2);
            date[0] = time.getYear();
            date[1] = time.getMonthOfYear() - 1;
            return date;
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_calendar, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            viewHolder mholder = (viewHolder) holder;
            int date[] = getYearAndMonth(position);
            mholder.monthView.setShowSelect(false);
            mholder.monthView.setNeedPreNext(false);
            mholder.monthView.init(mArray, date[0], date[1]);
            mholder.monthView.setPosition(position);
            mholder.monthView.invalidate();
            mholder.monthView.setOnDateClickListener(mMonthCalendarView);

            if (date[0] == todayYear && date[1] == todayMonth) {
                mholder.monthYear.setText(date[0] + "年");
                mholder.monthYearToday.setVisibility(VISIBLE);
                mholder.monthYearToday.setText((date[1] + 1) + "月");
            } else {
                mholder.monthYearToday.setVisibility(GONE);
                mholder.monthYear.setText(date[0] + "年" + (date[1] + 1) + "月");
            }
        }

        @Override
        public int getItemCount() {
            return mMonthCount;
        }
    }

    /**
     *
     */
    private class viewHolder extends ViewHolder {

        private MonthView monthView;
        private TextView monthYear, monthYearToday;

        public viewHolder(View itemView) {
            super(itemView);
            monthView = itemView.findViewById(R.id.mcvCalendar);
            monthYear = itemView.findViewById(R.id.monthYear);
            monthYearToday = itemView.findViewById(R.id.monthYearToday);
            monthYearToday.setVisibility(GONE);
        }
    }

    /**
     * 跳转到今天
     */
    public void setTodayToView() {
        scrollToPosition(getAdapter().getItemCount() / 2);
//        MonthView monthView = mMonthAdapter.getViews().get(mMonthAdapter.getMonthCount() / 2);
//        if (monthView != null) {
//            Calendar calendar = Calendar.getInstance();
//            monthView.clickThisMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
//        }
    }

    /**
     * 设置点击日期监听
     *
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }
}

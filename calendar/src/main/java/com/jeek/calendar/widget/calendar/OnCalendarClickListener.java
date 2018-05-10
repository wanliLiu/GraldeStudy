package com.jeek.calendar.widget.calendar;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public interface OnCalendarClickListener {
    /**
     * 点击选中
     *
     * @param year
     * @param month
     * @param day
     */
    void onClickDate(int year, int month, int day);

    /**
     * 分页滑动
     *
     * @param year
     * @param month
     * @param day
     */
    void onPageChange(int year, int month, int day);
}

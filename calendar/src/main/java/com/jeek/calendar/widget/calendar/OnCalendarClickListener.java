package com.jeek.calendar.widget.calendar;

import java.util.ArrayList;

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
     * @param events
     */
    void onClickDate(int year, int month, int day, ArrayList<Event> events);

    /**
     * 分页滑动
     *
     * @param year
     * @param month
     * @param day
     * @param events
     */
    void onPageChange(int year, int month, int day, ArrayList<Event> events);
}

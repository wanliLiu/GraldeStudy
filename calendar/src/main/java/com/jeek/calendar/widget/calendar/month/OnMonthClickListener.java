package com.jeek.calendar.widget.calendar.month;

import com.jeek.calendar.widget.calendar.Event;

import java.util.ArrayList;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public interface OnMonthClickListener {
    /**
     * 在一个月中,点击这个月的日期
     *
     * @param year
     * @param month
     * @param day
     * @param events
     */
    void onClickThisMonth(int year, int month, int day, ArrayList<Event> events);

    /**
     * 在一个月中,点击上一个月的日期
     *
     * @param year
     * @param month
     * @param day
     * @param events
     */
    void onClickLastMonth(int year, int month, int day, ArrayList<Event> events);

    /**
     * 在一个月中,点击下一个月的日期
     *
     * @param year
     * @param month
     * @param day
     * @param events
     */
    void onClickNextMonth(int year, int month, int day, ArrayList<Event> events);
}

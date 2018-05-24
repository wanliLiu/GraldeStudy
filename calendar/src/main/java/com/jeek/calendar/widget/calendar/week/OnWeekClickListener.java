package com.jeek.calendar.widget.calendar.week;

import com.jeek.calendar.widget.calendar.Event;

import java.util.ArrayList;

/**
 * Created by Jimmy on 2016/10/7 0007.
 */
public interface OnWeekClickListener {
    /**
     * @param year
     * @param month
     * @param day
     * @param events
     */
    void onClickDate(int year, int month, int day, ArrayList<Event> events);
}

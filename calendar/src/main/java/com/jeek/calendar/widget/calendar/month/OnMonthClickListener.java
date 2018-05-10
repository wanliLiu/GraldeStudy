package com.jeek.calendar.widget.calendar.month;

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
     */
    void onClickThisMonth(int year, int month, int day);

    /**
     * 在一个月中,点击上一个月的日期
     *
     * @param year
     * @param month
     * @param day
     */
    void onClickLastMonth(int year, int month, int day);

    /**
     * 在一个月中,点击下一个月的日期
     *
     * @param year
     * @param month
     * @param day
     */
    void onClickNextMonth(int year, int month, int day);
}

package com.jeek.calendar.widget.calendar;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.LocalDate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public class CalendarUtils {

    /**
     * 这个来改变是星期几还是，默认是星期一开始
     */
    public static int weekStart = Calendar.MONDAY;
    /**
     * 默认100 年 * 12 = 1200个月
     */
    public static int defaultMonthCount = 100 * 12;
    /**
     * 在年的基础上加上周的数量
     */
    public static int defaultWeekCount = defaultMonthCount * 4 + 10;


    private static CalendarUtils sUtils;
    private Map<String, int[]> sAllHolidays = new HashMap<>();
    private Map<String, List<Integer>> sMonthTaskHint = new HashMap<>();

    public static synchronized CalendarUtils getInstance(Context context) {
        if (sUtils == null) {
            sUtils = new CalendarUtils();
            sUtils.initAllHolidays(context);
        }
        return sUtils;
    }

    private void initAllHolidays(Context context) {
        try {
            InputStream is = context.getAssets().open(CalendarUtils.weekStart == Calendar.MONDAY ? "holiday_monday.json" : "holiday_sunday.json");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            sAllHolidays = new Gson().fromJson(baos.toString(), new TypeToken<Map<String, int[]>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> addTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            hints.removeAll(days); // 避免重复
            hints.addAll(days);
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.addAll(days);
        }
        return hints;
    }

    public List<Integer> removeTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.removeAll(days);
        }
        return hints;
    }

    public boolean addTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            hints.add(day);
            sUtils.sMonthTaskHint.put(key, hints);
            return true;
        } else {
            if (!hints.contains(day)) {
                hints.add(day);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean removeTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            if (hints.contains(day)) {
                Iterator<Integer> i = hints.iterator();
                while (i.hasNext()) {
                    Integer next = i.next();
                    if (next == day) {
                        i.remove();
                        break;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public List<Integer> getTaskHints(int year, int month) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        }
        return hints;
    }

    private static String hashKey(int year, int month) {
        return String.format("%s:%s", year, month);
    }

    /**
     * 通过年份和月份 得到当月的日子
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMonthDays(int year, int month) {
        LocalDate date = new LocalDate(year,month + 1,1);
        return date.dayOfMonth().getMaximumValue();
    }

    /**
     * 返回当前月份1号位于周几
     *
     * @param year  年份
     * @param month 月份，传入系统获取的，不需要正常的
     * @return 日：1		一：2		二：3		三：4		四：5		五：6		六：7
     */
    public static int getFirstDayWeek(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int getDayWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }


    /**
     * 获得两个日期距离几周
     *
     * @return
     */
    public static int getWeeksAgo(int lastYear, int lastMonth, int lastDay, int year, int month, int day) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.set(lastYear, lastMonth, lastDay);
        end.set(year, month, day);
        int week = start.get(Calendar.DAY_OF_WEEK);
        start.add(Calendar.DATE, -(findDayOffset(week) + 1));
        week = end.get(Calendar.DAY_OF_WEEK);
        end.add(Calendar.DATE, 7 - findDayOffset(week) - 1);
        float v = (end.getTimeInMillis() - start.getTimeInMillis()) / (3600 * 1000 * 24 * 7 * 1.0f);
        return (int) (v - 1);
    }

    /**
     * 获得两个日期距离几个月
     *
     * @return
     */
    public static int getMonthsAgo(int lastYear, int lastMonth, int year, int month) {
        return (year - lastYear) * 12 + (month - lastMonth);
    }

    /**
     * 算出对应天所占的行数,0-----5
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static int getWeekRow(int year, int month, int day) {
        int week = getFirstDayWeek(year, month);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        int lastWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (lastWeek == 7)
            day--;
        return (day + week - 1) / 7;
    }

    /**
     * 根据国历获取假期
     *
     * @return
     */
    public static String getHolidayFromSolar(int year, int month, int day) {
        String message = "";
        if (month == 0 && day == 1) {
            message = "元旦";
        } else if (month == 1 && day == 14) {
            message = "情人节";
        } else if (month == 2 && day == 8) {
            message = "妇女节";
        } else if (month == 2 && day == 12) {
            message = "植树节";
        } else if (month == 3) {
            if (day == 1) {
                message = "愚人节";
            } else if (day >= 4 && day <= 6) {
                if (year <= 1999) {
                    int compare = (int) (((year - 1900) * 0.2422 + 5.59) - ((year - 1900) / 4));
                    if (compare == day) {
                        message = "清明节";
                    }
                } else {
                    int compare = (int) (((year - 2000) * 0.2422 + 4.81) - ((year - 2000) / 4));
                    if (compare == day) {
                        message = "清明节";
                    }
                }
            }
        } else if (month == 4 && day == 1) {
            message = "劳动节";
        } else if (month == 4 && day == 4) {
            message = "青年节";
        } else if (month == 4 && day == 12) {
            message = "护士节";
        } else if (month == 5 && day == 1) {
            message = "儿童节";
        } else if (month == 6 && day == 1) {
            message = "建党节";
        } else if (month == 7 && day == 1) {
            message = "建军节";
        } else if (month == 8 && day == 10) {
            message = "教师节";
        } else if (month == 9 && day == 1) {
            message = "国庆节";
        } else if (month == 10 && day == 11) {
            message = "光棍节";
        } else if (month == 11 && day == 25) {
            message = "圣诞节";
        }
        return message;
    }

    public int[] getHolidays(int year, int month) {
        int holidays[];
        if (sUtils.sAllHolidays != null) {
            holidays = sUtils.sAllHolidays.get(year + "" + month);
            if (holidays == null) {
                holidays = new int[42];
            }
        } else {
            holidays = new int[42];
        }
        return holidays;
    }

    /**
     * 不管是星期日开始还是星期一开始，算出起始位置，0------6
     *
     * @param mDayOfWeek 对应号的星期几,算出在周日和周一都相同的索引位置
     * @return
     */
    public static int findDayOffset(int mDayOfWeek) {
        return (mDayOfWeek < CalendarUtils.weekStart ? (mDayOfWeek + 7) : mDayOfWeek) - CalendarUtils.weekStart;
    }

    /**
     * 一个月有几行
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMonthRows(int year, int month) {
        int mNumCells = getMonthDays(year, month);
        int offset = findDayOffset(getFirstDayWeek(year, month));
        int dividend = (offset + mNumCells) / 7;
        int remainder = (offset + mNumCells) % 7;
        return (dividend + (remainder > 0 ? 1 : 0));
    }

}


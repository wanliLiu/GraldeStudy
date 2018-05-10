package com.jeek.calendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jeek.calendar.widget.calendar.CalendarListView;
import com.jeek.calendar.widget.calendar.OnCalendarClickListener;
import com.jeek.calendar.widget.calendar.week.WeekCalendarView;
import com.jimmy.common.util.ToastUtils;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        WeekCalendarView week = findViewById(R.id.weekView);
        week.setOnCalendarClickListener(new OnCalendarClickListener() {
            @Override
            public void onClickDate(int year, int month, int day) {
                ToastUtils.showLongToast(TestActivity.this, year + "-" + (month + 1) + "-" + day);
            }

            @Override
            public void onPageChange(int year, int month, int day) {
                ToastUtils.showLongToast(TestActivity.this, year + "-" + (month + 1) + "-" + day);
            }
        });

        CalendarListView mont = findViewById(R.id.mont);
        mont.setOnCalendarClickListener(new OnCalendarClickListener() {
            @Override
            public void onClickDate(int year, int month, int day) {
                ToastUtils.showLongToast(TestActivity.this, year + "-" + (month + 1) + "-" + day);
            }

            @Override
            public void onPageChange(int year, int month, int day) {

            }
        });
    }
}

package com.jeek.calendar.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jeek.calendar.R;
import com.jeek.calendar.TestActivity;
import com.jeek.calendar.adapter.EventSetAdapter;
import com.jeek.calendar.fragment.EventSetFragment;
import com.jeek.calendar.fragment.ScheduleFragment;
import com.jeek.calendar.task.eventset.LoadEventSetTask;
import com.jimmy.common.base.app.BaseActivity;
import com.jimmy.common.base.app.BaseFragment;
import com.jimmy.common.bean.EventSet;
import com.jimmy.common.listener.OnTaskFinishedListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.functions.Consumer;

public class MainActivity extends BaseActivity implements View.OnClickListener, OnTaskFinishedListener<List<EventSet>> {

    public static int ADD_EVENT_SET_CODE = 1;
    public static String ADD_EVENT_SET_ACTION = "action.add.event.set";

    private DrawerLayout dlMain;
    private LinearLayout llTitleDate;
    private TextView tvTitleMonth, tvTitleDay, tvTitle;
    private RecyclerView rvMenuEventSetList;

    private EventSetAdapter mEventSetAdapter;
    private List<EventSet> mEventSets;

    private BaseFragment mScheduleFragment, mEventSetFragment;
    private EventSet mCurrentEventSet;
    private AddEventSetBroadcastReceiver mAddEventSetBroadcastReceiver;

    private long[] mNotes = new long[2];
    private String[] mMonthText;
    private int mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay;

    private RxPermissions permissions;

    @Override
    protected void bindView() {

        setContentView(R.layout.activity_main);

        permissions = new RxPermissions(this);
        init();
    }


    private void permissionCheck() {
        permissions.request(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (!aBoolean) {
                            finish();
                        }
                    }
                });
    }

    /**
     *
     */
    private void init() {

        dlMain = searchViewById(R.id.dlMain);
        llTitleDate = searchViewById(R.id.llTitleDate);
        tvTitleMonth = searchViewById(R.id.tvTitleMonth);
        tvTitleDay = searchViewById(R.id.tvTitleDay);
        tvTitle = searchViewById(R.id.tvTitle);
        rvMenuEventSetList = searchViewById(R.id.rvMenuEventSetList);

//        List<Integer> integers = new ArrayList<>();
//        integers.add(1);
//        integers.add(2);
//        integers.add(3);
//        integers.add(4);
//        integers.add(5);
//        integers.add(6);
//        integers.add(7);
//        CalendarUtils.getInstance(this).addTaskHints(2018, 4, integers);
//        CalendarUtils.getInstance(this).addTaskHints(2018, 6, integers);


        searchViewById(R.id.ivMainMenu).setOnClickListener(this);
        searchViewById(R.id.llMenuSchedule).setOnClickListener(this);
        searchViewById(R.id.llMenuNoCategory).setOnClickListener(this);
        searchViewById(R.id.tvMenuAddEventSet).setOnClickListener(this);
        initUi();
        initEventSetList();
        gotoScheduleFragment();
        initBroadcastReceiver();
    }

    private void initBroadcastReceiver() {
        if (mAddEventSetBroadcastReceiver == null) {
            mAddEventSetBroadcastReceiver = new AddEventSetBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ADD_EVENT_SET_ACTION);
            registerReceiver(mAddEventSetBroadcastReceiver, filter);
        }
    }

    private void initEventSetList() {
        mEventSets = new ArrayList<>();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMenuEventSetList.setLayoutManager(manager);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        rvMenuEventSetList.setItemAnimator(itemAnimator);
        mEventSetAdapter = new EventSetAdapter(this, mEventSets);
        rvMenuEventSetList.setAdapter(mEventSetAdapter);
    }

    private void initUi() {
        dlMain.setScrimColor(Color.TRANSPARENT);
        mMonthText = getResources().getStringArray(R.array.calendar_month);
        llTitleDate.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.GONE);
        tvTitleMonth.setText(mMonthText[Calendar.getInstance().get(Calendar.MONTH)]);
        tvTitleDay.setText(getString(R.string.calendar_today));
        if (Build.VERSION.SDK_INT < 19) {
            TextView tvMenuTitle = searchViewById(R.id.tvMenuTitle);
            tvMenuTitle.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    protected void initData() {
        super.initData();

        permissionCheck();

        resetMainTitleDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        new LoadEventSetTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void resetMainTitleDate(int year, int month, int day) {
        llTitleDate.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.GONE);
        Calendar calendar = Calendar.getInstance();
        if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) &&
                day == calendar.get(Calendar.DAY_OF_MONTH)) {
            tvTitleMonth.setText(mMonthText[month]);
            tvTitleDay.setText(getString(R.string.calendar_today));
        } else {
            if (year == calendar.get(Calendar.YEAR)) {
                tvTitleMonth.setText(mMonthText[month]);
            } else {
                tvTitleMonth.setText(String.format("%s%s", String.format(getString(R.string.calendar_year), year),
                        mMonthText[month]));
            }
            tvTitleDay.setText(String.format(getString(R.string.calendar_day), day));
        }
        setCurrentSelectDate(year, month, day);
    }

    private void resetTitleText(String name) {
        llTitleDate.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(name);
    }

    private void setCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivMainMenu:
//                dlMain.openDrawer(Gravity.START);
                startActivity(new Intent(this, TestActivity.class));
                break;
            case R.id.llMenuSchedule:
                gotoScheduleFragment();
                break;
            case R.id.llMenuNoCategory:
                mCurrentEventSet = new EventSet();
                mCurrentEventSet.setName(getString(R.string.menu_no_category));
                gotoEventSetFragment(mCurrentEventSet);
                break;
            case R.id.tvMenuAddEventSet:
                gotoAddEventSetActivity();
                break;
        }
    }

    private void gotoScheduleFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        if (mScheduleFragment == null) {
            mScheduleFragment = ScheduleFragment.getInstance();
            ft.add(R.id.flMainContainer, mScheduleFragment);
        }
        if (mEventSetFragment != null)
            ft.hide(mEventSetFragment);
        ft.show(mScheduleFragment);
        ft.commit();
        llTitleDate.setVisibility(View.VISIBLE);
        tvTitle.setVisibility(View.GONE);
        dlMain.closeDrawer(Gravity.START);
    }

    public void gotoEventSetFragment(EventSet eventSet) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        if (mCurrentEventSet != eventSet || eventSet.getId() == 0) {
            if (mEventSetFragment != null)
                ft.remove(mEventSetFragment);
            mEventSetFragment = EventSetFragment.getInstance(eventSet);
            ft.add(R.id.flMainContainer, mEventSetFragment);
        }
        ft.hide(mScheduleFragment);
        ft.show(mEventSetFragment);
        ft.commit();
        resetTitleText(eventSet.getName());
        dlMain.closeDrawer(Gravity.START);
        mCurrentEventSet = eventSet;
    }

    private void gotoAddEventSetActivity() {
        startActivityForResult(new Intent(this, AddEventSetActivity.class), ADD_EVENT_SET_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_EVENT_SET_CODE) {
            if (resultCode == AddEventSetActivity.ADD_EVENT_SET_FINISH) {
                EventSet eventSet = (EventSet) data.getSerializableExtra(AddEventSetActivity.EVENT_SET_OBJ);
                if (eventSet != null)
                    mEventSetAdapter.insertItem(eventSet);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (dlMain.isDrawerOpen(Gravity.START)) {
            dlMain.closeDrawer(Gravity.START);
        } else {
            System.arraycopy(mNotes, 1, mNotes, 0, mNotes.length - 1);
            mNotes[mNotes.length - 1] = SystemClock.uptimeMillis();
            if (SystemClock.uptimeMillis() - mNotes[0] < 1000) {
                finish();
            } else {
                Toast.makeText(this, getString(R.string.exit_app_hint), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAddEventSetBroadcastReceiver != null) {
            unregisterReceiver(mAddEventSetBroadcastReceiver);
            mAddEventSetBroadcastReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onTaskFinished(List<EventSet> data) {
        mEventSetAdapter.changeAllData(data);
    }

    private class AddEventSetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ADD_EVENT_SET_ACTION.equals(intent.getAction())) {
                EventSet eventSet = (EventSet) intent.getSerializableExtra(AddEventSetActivity.EVENT_SET_OBJ);
                if (eventSet != null)
                    mEventSetAdapter.insertItem(eventSet);
            }
        }
    }

}

package com.jeek.calendar;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * @author Soli
 * @Time 18-5-7 下午3:48
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}

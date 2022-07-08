package com.dqsmart.example.zigbee;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.dqsmart.zigbee.DqsZbNwkManager;

import java.util.Arrays;

public class AppApplication extends Application implements LifecycleObserver {

    private static final String TAG = "AppApplication";

    private static AppApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        DqsZbNwkManager.init(this);
    }

    public static AppApplication getAppContext() {
        return sApplication;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void onAppBackgrounded() {
        Log.d(TAG, "App in background");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void onAppForegrounded() {
        Log.d(TAG, "App in foreground");
    }
}

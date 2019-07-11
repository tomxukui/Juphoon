package com.juphoon.cloud;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.juphoon.cloud.wrapper.JCManager;

import static android.content.Context.ACTIVITY_SERVICE;

public class JuphoonClient {

    private Application mApplication;
    private int mFrontActivityCount;

    private JuphoonClient() {
    }

    public static JuphoonClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final JuphoonClient INSTANCE = new JuphoonClient();
    }

    public void init(Application application, String appKey) {
        mApplication = application;

        String processName = getCurProcessName(application);
        String packname = application.getPackageName();

        if (TextUtils.equals(processName, packname)) {
            JCManager.getInstance().init(application, appKey);
            mFrontActivityCount = 0;

            application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    if (mFrontActivityCount == 0) {
                        JCManager.getInstance().client.setForeground(true);
                    }
                    mFrontActivityCount++;
                }

                @Override
                public void onActivityResumed(Activity activity) {
                }

                @Override
                public void onActivityPaused(Activity activity) {
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    mFrontActivityCount--;

                    if (mFrontActivityCount == 0) {
                        JCManager.getInstance().client.setForeground(false);
                    }
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }

            });
        }
    }

    public Context getContext() {
        return mApplication.getApplicationContext();
    }

    private String getCurProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == android.os.Process.myPid()) {
                return appProcess.processName;
            }
        }
        return "";
    }

}
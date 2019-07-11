package com.xukui.juphoon;

import android.app.Application;

import com.juphoon.cloud.JuphoonClient;

public class Mapp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JuphoonClient.getInstance().init(this, "3feb50d8605627f795625095");
    }

}
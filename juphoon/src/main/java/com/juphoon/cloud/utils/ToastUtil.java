package com.juphoon.cloud.utils;

import android.widget.Toast;

import com.juphoon.cloud.JuphoonClient;

public class ToastUtil {

    public static void show(String msg) {
        Toast.makeText(JuphoonClient.getInstance().getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}

package com.juphoon.cloud.utils;

import android.view.Gravity;
import android.widget.Toast;

import com.juphoon.cloud.JuphoonClient;

public class ToastUtil {

    public static void showShort(String message) {
        Toast toast = Toast.makeText(JuphoonClient.getInstance().getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
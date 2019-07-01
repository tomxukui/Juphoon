package com.juphoon.cloud.utils;

import com.juphoon.cloud.JuphoonClient;

public class StringUtil {

    public static String getString(int resId) {
        return JuphoonClient.getInstance().getContext().getString(resId);
    }

}

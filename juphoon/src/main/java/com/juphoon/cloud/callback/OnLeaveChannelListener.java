package com.juphoon.cloud.callback;

/**
 * Created by xukui on 2019-04-18.
 */
public interface OnLeaveChannelListener {

    void onStart();

    void onSuccess(String message);

    void onFailure(String message);

}

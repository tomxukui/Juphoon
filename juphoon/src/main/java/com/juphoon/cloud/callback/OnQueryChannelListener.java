package com.juphoon.cloud.callback;

import com.juphoon.cloud.JCMediaChannelQueryInfo;

/**
 * Created by xukui on 2019-04-18.
 */
public interface OnQueryChannelListener {

    void onStart();

    void onSuccess(String message, JCMediaChannelQueryInfo queryInfo);

    void onFailure(String message);

}

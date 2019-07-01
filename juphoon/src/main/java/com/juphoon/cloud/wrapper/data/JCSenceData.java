package com.juphoon.cloud.wrapper.data;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.juphoon.cloud.JCMediaChannel;
import com.juphoon.cloud.JCMediaChannelParticipant;
import com.juphoon.cloud.JCMediaDeviceVideoCanvas;
import com.juphoon.cloud.wrapper.JCChannelUtil;
import com.juphoon.cloud.wrapper.JCManager;

import java.io.Serializable;

public class JCSenceData implements Serializable {

    private FrameLayout mFrameLayout;
    private JCMediaChannelParticipant mParticipant;
    private JCMediaDeviceVideoCanvas mCanvas;

    public JCSenceData(Context context) {
        mFrameLayout = new FrameLayout(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mFrameLayout.setLayoutParams(layoutParams);
    }

    public FrameLayout getView() {
        return mFrameLayout;
    }

    public JCMediaChannelParticipant getParticipant() {
        return mParticipant;
    }

    public void setParticipant(JCMediaChannelParticipant participant) {
        mParticipant = participant;
    }

    public JCMediaDeviceVideoCanvas getCanvas() {
        return mCanvas;
    }

    public void setCanvas(JCMediaDeviceVideoCanvas canvas) {
        mCanvas = canvas;
    }

    /**
     * 重置
     */
    public void reset() {
        if (mCanvas != null) {
            // 关闭视频请求
            if (mParticipant != null && (!JCChannelUtil.isSelf(mParticipant))) {
                JCManager.getInstance().mediaChannel.requestVideo(mParticipant, JCMediaChannel.PICTURESIZE_NONE);
            }

            JCManager.getInstance().mediaDevice.stopVideo(mCanvas);
            mFrameLayout.removeView(mCanvas.getVideoView());
            mCanvas = null;
        }
    }

    /**
     * 移除
     */
    public void delete(ViewGroup parent) {
        reset();

        if (parent != null) {
            parent.removeView(mFrameLayout);
        }
    }

    /**
     * 释放
     */
    public void release(ViewGroup parent) {
        delete(parent);

        mFrameLayout = null;
        mParticipant = null;
    }

}
package com.juphoon.cloud.wrapper;

import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.juphoon.cloud.JCMediaChannelParticipant;
import com.juphoon.cloud.JCMediaDevice;
import com.juphoon.cloud.wrapper.data.JCSenceData;

import java.util.List;

public class JCChannelUtil {

    /**
     * 判断是否是本人
     */
    public static boolean isSelf(JCMediaChannelParticipant participant) {
        if (participant == null) {
            return false;

        } else {
            return TextUtils.equals(participant.getUserId(), JCManager.getInstance().client.getUserId());
        }
    }

    /**
     * 设置视角控件
     */
    public static void setSceneView(final ViewGroup parent, final List<JCMediaChannelParticipant> participants, final List<JCSenceData> senceDatas, final int videoSize) {
        if (parent == null || participants == null || senceDatas == null) {
            return;
        }

        if (parent.getWidth() == 0) {
            parent.postDelayed(new Runnable() {

                @Override
                public void run() {
                    setSceneView(parent, participants, senceDatas, videoSize);
                }

            }, 500);
            return;
        }

        List<JCConfUtils.SubViewRect> subViewRects = JCConfUtils.caclSubViewRect(parent.getWidth(), parent.getHeight(), participants.size());

        for (int i = 0; ; i++) {
            if (i < participants.size()) {
                JCMediaChannelParticipant participant = participants.get(i);
                JCConfUtils.SubViewRect subViewRect = subViewRects.get(i);
                JCSenceData item;
                if (senceDatas.size() <= i) {
                    item = new JCSenceData(parent.getContext());
                    senceDatas.add(item);
                    parent.addView(item.getView());

                } else {
                    item = senceDatas.get(i);
                }

                if (item.getParticipant() != participant) {
                    item.reset();
                    item.setParticipant(participant);
                }

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subViewRect.width, subViewRect.height);
                params.setMargins(subViewRect.x, subViewRect.y, 0, 0);
                item.getView().setLayoutParams(params);
                continue;

            } else if (i < senceDatas.size()) {
                for (int j = senceDatas.size() - 1; j >= i; j--) {
                    senceDatas.get(j).delete(parent);
                    senceDatas.remove(j);
                }
            }
            break;
        }

        updateSceneView(senceDatas, videoSize);
    }

    /**
     * 更新视角控件
     */
    public static void updateSceneView(List<JCSenceData> senceDatas, int videoSize) {
        if (senceDatas == null) {
            return;
        }

        for (JCSenceData senceData : senceDatas) {
            if (isSelf(senceData.getParticipant())) {
                if (senceData.getParticipant().isVideo()) {
                    if (senceData.getCanvas() == null) {
                        senceData.setCanvas(JCManager.getInstance().mediaDevice.startCameraVideo(JCMediaDevice.RENDER_FULL_CONTENT));

                        if (senceData.getCanvas() != null) {
                            senceData.getView().addView(senceData.getCanvas().getVideoView(), 0);
                        }
                    }
                }

            } else {
                if (senceData.getParticipant().isVideo()) {
                    if (senceData.getCanvas() == null) {
                        JCManager.getInstance().mediaChannel.requestVideo(senceData.getParticipant(), videoSize);
                        senceData.setCanvas(JCManager.getInstance().mediaDevice.startVideo(senceData.getParticipant().getRenderId(), JCMediaDevice.RENDER_FULL_CONTENT));
                        senceData.getView().addView(senceData.getCanvas().getVideoView(), 0);
                    }
                }
            }

            // 当用户没有视频或者显示屏幕分享是关闭视频流
            if (!senceData.getParticipant().isVideo()) {
                if (senceData.getCanvas() != null) {
                    senceData.reset();
                }
            }
        }
    }

    /**
     * 移除视角控件
     */
    public static void removeSceneView(List<JCSenceData> senceDatas) {
        if (senceDatas == null) {
            return;
        }

        for (JCSenceData senceData : senceDatas) {
            if (senceData.getCanvas() != null) {
                senceData.reset();
            }
        }
    }

    /**
     * 释放视角控件
     */
    public static void releaseSceneView(ViewGroup parent, List<JCSenceData> senceDatas) {
        if (parent == null || senceDatas == null) {
            return;
        }

        for (JCSenceData senceData : senceDatas) {
            senceData.release(parent);
        }
    }

}
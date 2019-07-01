package com.juphoon.cloud.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.juphoon.cloud.JCMediaChannel;
import com.juphoon.cloud.JCMediaChannelParticipant;
import com.juphoon.cloud.doodle.R;
import com.juphoon.cloud.wrapper.JCChannelUtil;
import com.juphoon.cloud.wrapper.JCManager;
import com.juphoon.cloud.wrapper.data.JCSenceData;
import com.juphoon.cloud.wrapper.event.JCEvent;
import com.juphoon.cloud.wrapper.event.JCJoinEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频会议全屏页面
 */
public class ConferenceActivity extends AppCompatActivity {

    private static final int VIDEO_SIZE = JCMediaChannel.PICTURESIZE_LARGE;

    private FrameLayout frame_scene;
    private Button btn_rotateScene;
    private Button btn_switchCamera;
    private Button btn_sendAudio;
    private Button btn_sendVideo;
    private Button btn_playAudio;
    private Button btn_speaker;
    private Button btn_leaveChannel;
    private Button btn_stopChannel;
    private Button btn_back;

    private List<JCSenceData> mSenceDatas = new ArrayList<>();
    private int mAngleIndex = 0;//旋转角度计数

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.juphoon_activity_conference);
        initView();
        setView();

        EventBus.getDefault().register(this);
        checkChannel();
    }

    private void initView() {
        frame_scene = findViewById(R.id.frame_scene);
        btn_rotateScene = findViewById(R.id.btn_rotateScene);
        btn_switchCamera = findViewById(R.id.btn_switchCamera);
        btn_sendAudio = findViewById(R.id.btn_sendAudio);
        btn_sendVideo = findViewById(R.id.btn_sendVideo);
        btn_playAudio = findViewById(R.id.btn_playAudio);
        btn_speaker = findViewById(R.id.btn_speaker);
        btn_leaveChannel = findViewById(R.id.btn_leaveChannel);
        btn_stopChannel = findViewById(R.id.btn_stopChannel);
        btn_back = findViewById(R.id.btn_back);
    }

    private void setView() {
        btn_rotateScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateScene();
            }
        });

        btn_switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        btn_sendAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAudio();
            }
        });

        btn_sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVideo();
            }
        });

        btn_playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        btn_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSpeaker();
            }
        });

        btn_leaveChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveChannel();
            }
        });

        btn_stopChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopChannel();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPage();
            }
        });
    }

    @Override
    public void onBackPressed() {
        leaveChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setSceneViews();
        setControlBtns();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        releaseSceneViews();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            int direction = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
            int flags = AudioManager.FX_FOCUS_NAVIGATION_UP;
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, direction, flags);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 旋转画面
     */
    private void rotateScene() {
        int angle = 90 * ++mAngleIndex % 360;
        for (JCSenceData senceData : mSenceDatas) {
            if (senceData.getCanvas() != null) {
                senceData.getCanvas().rotate(angle);
            }
        }
    }

    private void enableSpeaker() {
        JCManager.getInstance().mediaDevice.enableSpeaker(!JCManager.getInstance().mediaDevice.isSpeakerOn());
        setControlBtns();
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        JCManager.getInstance().mediaDevice.switchCamera();
    }

    /**
     * 是否发送语音
     */
    private void sendAudio() {
        JCManager.getInstance().mediaChannel.enableUploadAudioStream(!JCManager.getInstance().mediaChannel.getUploadLocalAudio());
    }

    /**
     * 是否发送视频
     */
    private void sendVideo() {
        JCManager.getInstance().mediaChannel.enableUploadVideoStream(!JCManager.getInstance().mediaChannel.getUploadLocalVideo());
    }

    /**
     * 是否静音
     */
    private void playAudio() {
        JCManager.getInstance().mediaChannel.enableAudioOutput(!JCManager.getInstance().mediaChannel.getAudioOutput());
    }

    /**
     * 离开
     */
    private void leaveChannel() {
        JCManager.getInstance().mediaChannel.leave();
    }

    /**
     * 结束
     */
    private void stopChannel() {
        JCManager.getInstance().mediaChannel.stop();
    }

    /**
     * 返回页面
     */
    private void backPage() {
        finish();
    }

    /**
     * 检测参加会议是否成功
     */
    private void checkChannel() {
        if (JCManager.getInstance().mediaChannel.getState() == JCMediaChannel.STATE_IDLE) {
            showFailDialog(JCMediaChannel.REASON_OTHER);
        }
    }

    /**
     * 显示参加会议失败的对话框
     */
    private void showFailDialog(int reason) {
        String message = (reason == JCMediaChannel.REASON_INVALID_PASSWORD ? "会议口令错误" : "其他错误");

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .create()
                .show();
    }

    /**
     * 更新控制按钮
     */
    private void setControlBtns() {
        btn_playAudio.setSelected(JCManager.getInstance().mediaChannel.getAudioOutput());
        btn_sendAudio.setSelected(JCManager.getInstance().mediaChannel.getUploadLocalAudio());
        btn_sendVideo.setSelected(JCManager.getInstance().mediaChannel.getUploadLocalVideo());
        btn_speaker.setSelected(JCManager.getInstance().mediaDevice.isSpeakerOn());
    }

    private void setSceneViews() {
        List<JCMediaChannelParticipant> participants = JCManager.getInstance().mediaChannel.getParticipants();

        JCChannelUtil.setSceneView(frame_scene, participants, mSenceDatas, VIDEO_SIZE);
    }

    private void releaseSceneViews() {
        JCChannelUtil.releaseSceneView(frame_scene, mSenceDatas);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJCEvent(JCEvent event) {
        if (event.getEventType() == JCEvent.EventType.CONFERENCE_JOIN) {
            JCJoinEvent join = (JCJoinEvent) event;

            if (join.result) {
                setSceneViews();

            } else {
                showFailDialog(join.reason);
            }

        } else if (event.getEventType() == JCEvent.EventType.CONFERENCE_PARTP_JOIN || event.getEventType() == JCEvent.EventType.CONFERENCE_PARTP_LEAVE) {
            setSceneViews();

        } else if (event.getEventType() == JCEvent.EventType.CONFERENCE_PARTP_UPDATE) {
            JCChannelUtil.updateSceneView(mSenceDatas, VIDEO_SIZE);

        } else if (event.getEventType() == JCEvent.EventType.CONFERENCE_PROP_CHANGE) {
            setControlBtns();

        } else if (event.getEventType() == JCEvent.EventType.CONFERENCE_LEAVE) {
            finish();
        }
    }

}
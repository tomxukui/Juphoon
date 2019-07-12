package com.xukui.juphoon.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.juphoon.cloud.callback.OnJoinChannelListener;
import com.juphoon.cloud.callback.OnLoginListener;
import com.juphoon.cloud.callback.OnLogoutListener;
import com.juphoon.cloud.callback.OnStopChannelListener;
import com.juphoon.cloud.ui.ConferenceActivity;
import com.juphoon.cloud.wrapper.JCManager;
import com.xukui.juphoon.R;
import com.xukui.juphoon.permission.PermissionUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_userid;
    private EditText et_pwd;
    private Button btn_login;
    private EditText et_channelId;
    private EditText et_channelPwd;
    private Button btn_joinChannel;
    private Button btn_login_join;
    private Button btn_stopChannel;
    private Button btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setView();
    }

    private void initView() {
        et_userid = findViewById(R.id.et_userid);
        et_pwd = findViewById(R.id.et_pwd);
        btn_login = findViewById(R.id.btn_login);
        et_channelId = findViewById(R.id.et_channelId);
        et_channelPwd = findViewById(R.id.et_channelPwd);
        btn_joinChannel = findViewById(R.id.btn_joinChannel);
        btn_login_join = findViewById(R.id.btn_login_join);
        btn_stopChannel = findViewById(R.id.btn_stopChannel);
        btn_logout = findViewById(R.id.btn_logout);
    }

    private void setView() {
        btn_login.setOnClickListener(v -> requestPermission(data -> login(), Permission.Group.CAMERA, Permission.Group.STORAGE, Permission.Group.MICROPHONE));
        btn_joinChannel.setOnClickListener(v -> requestPermission(data -> joinChannel(), Permission.Group.CAMERA, Permission.Group.STORAGE, Permission.Group.MICROPHONE));
        btn_login_join.setOnClickListener(v -> requestPermission(data -> login_join(), Permission.Group.CAMERA, Permission.Group.STORAGE, Permission.Group.MICROPHONE));
        btn_stopChannel.setOnClickListener(v -> stopChannel());
        btn_logout.setOnClickListener(v -> logout());
    }

    /**
     * 登录
     */
    private void login() {
        String userid = et_userid.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();

        JCManager.getInstance().login(userid, pwd, new OnLoginListener() {

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onSuccess(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * 参加问诊
     */
    private void joinChannel() {
        String channelId = et_channelId.getText().toString().trim();
        String channelPwd = et_channelPwd.getText().toString().trim();

        JCManager.getInstance().joinChannel(channelId, channelPwd, new OnJoinChannelListener() {

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onSuccess(String message) {
                dialog.dismiss();

                Intent intent = ConferenceActivity.buildIntent(MainActivity.this, true, true);
                startActivity(intent);
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * 登录并加入问诊
     */
    private void login_join() {
        String userid = et_userid.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();
        String channelId = et_channelId.getText().toString().trim();
        String channelPwd = et_channelPwd.getText().toString().trim();

        JCManager.getInstance().login(userid, pwd, new OnLoginListener() {

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onSuccess(String message) {
                JCManager.getInstance().joinChannel(channelId, channelPwd, new OnJoinChannelListener() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(String message) {
                        dialog.dismiss();

                        Intent intent = ConferenceActivity.buildIntent(MainActivity.this, true, true);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String message) {
                        dialog.dismiss();

                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                });
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * 结束问诊
     */
    private void stopChannel() {
        JCManager.getInstance().stopChannel(new OnStopChannelListener() {

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onSuccess(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * 退出账号
     */
    private void logout() {
        JCManager.getInstance().logout(new OnLogoutListener() {

            ProgressDialog dialog = new ProgressDialog(MainActivity.this);

            @Override
            public void onStart() {
                dialog.show();
            }

            @Override
            public void onSuccess(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                dialog.dismiss();

                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void requestPermission(Action<List<String>> granted, @Nullable Action<List<String>> denied, String... permissions) {
        PermissionUtil.requestPermission(this, AndPermission.with(this), granted, denied, permissions);
    }

    private void requestPermission(Action<List<String>> granted, @Nullable Action<List<String>> denied, String[]... groups) {
        PermissionUtil.requestPermission(this, AndPermission.with(this), granted, denied, groups);
    }

    private void requestPermission(Action<List<String>> granted, String... permissions) {
        requestPermission(granted, data -> Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show(), permissions);
    }

    private void requestPermission(Action<List<String>> granted, String[]... groups) {
        requestPermission(granted, data -> Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show(), groups);
    }

}
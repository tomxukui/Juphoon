package com.juphoon.cloud.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.juphoon.cloud.JCAccount;
import com.juphoon.cloud.JCCall;
import com.juphoon.cloud.JCCallCallback;
import com.juphoon.cloud.JCCallItem;
import com.juphoon.cloud.JCClient;
import com.juphoon.cloud.JCClientCallback;
import com.juphoon.cloud.JCConfig;
import com.juphoon.cloud.JCGroup;
import com.juphoon.cloud.JCGroupCallback;
import com.juphoon.cloud.JCGroupItem;
import com.juphoon.cloud.JCGroupMember;
import com.juphoon.cloud.JCMediaChannel;
import com.juphoon.cloud.JCMediaChannelCallback;
import com.juphoon.cloud.JCMediaChannelParticipant;
import com.juphoon.cloud.JCMediaChannelQueryInfo;
import com.juphoon.cloud.JCMediaDevice;
import com.juphoon.cloud.JCMediaDeviceCallback;
import com.juphoon.cloud.JCMediaDeviceVideoCanvas;
import com.juphoon.cloud.JCMessageChannel;
import com.juphoon.cloud.JCMessageChannelCallback;
import com.juphoon.cloud.JCMessageChannelItem;
import com.juphoon.cloud.JCPush;
import com.juphoon.cloud.JCStorage;
import com.juphoon.cloud.JCStorageCallback;
import com.juphoon.cloud.JCStorageItem;
import com.juphoon.cloud.callback.OnJoinChannelListener;
import com.juphoon.cloud.callback.OnLeaveChannelListener;
import com.juphoon.cloud.callback.OnLoginListener;
import com.juphoon.cloud.callback.OnLogoutListener;
import com.juphoon.cloud.callback.OnQueryChannelListener;
import com.juphoon.cloud.callback.OnStopChannelListener;
import com.juphoon.cloud.doodle.R;
import com.juphoon.cloud.utils.StringUtil;
import com.juphoon.cloud.utils.ToastUtil;
import com.juphoon.cloud.wrapper.data.JCGroupData;
import com.juphoon.cloud.wrapper.data.JCMessageData;
import com.juphoon.cloud.wrapper.event.JCAccountQueryStatusEvent;
import com.juphoon.cloud.wrapper.event.JCCallMessageEvent;
import com.juphoon.cloud.wrapper.event.JCConfMessageEvent;
import com.juphoon.cloud.wrapper.event.JCConfQueryEvent;
import com.juphoon.cloud.wrapper.event.JCConfStopEvent;
import com.juphoon.cloud.wrapper.event.JCEvent;
import com.juphoon.cloud.wrapper.event.JCJoinEvent;
import com.juphoon.cloud.wrapper.event.JCLoginEvent;
import com.juphoon.cloud.wrapper.event.JCMessageEvent;
import com.juphoon.cloud.wrapper.event.JCStorageEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JCManager {

    private static final boolean PSTN_MODE = false;//会议的Pstn落地模式

    public JCClient client;
    public JCCall call;
    public JCMediaDevice mediaDevice;
    public JCMediaChannel mediaChannel;
    public JCMessageChannel messageChannel;
    public JCStorage storage;
    public JCGroup group;
    public JCPush push;
    public JCAccount account;
    public JCConfig config;

    private List<OnLoginListener> mOnLoginListeners = new ArrayList<>();
    private List<OnLogoutListener> mOnLogoutListeners = new ArrayList<>();
    private List<OnQueryChannelListener> mOnQueryChannelListeners = new ArrayList<>();
    private List<OnJoinChannelListener> mOnJoinChannelListeners = new ArrayList<>();
    private List<OnStopChannelListener> mOnStopChannelListeners = new ArrayList<>();
    private List<OnLeaveChannelListener> mOnLeaveChannelListeners = new ArrayList<>();

    public static JCManager getInstance() {
        return JCManagerHolder.INSTANCE;
    }

    public boolean init(Context context, String appKey) {
        client = initJCClient(context, appKey);
        mediaDevice = initJCMediaDevice(client);
        mediaChannel = initJCMediaChannel(client, mediaDevice);
        call = initJCCall(client, mediaDevice);
        messageChannel = initJCMessageChannel(client);
        storage = initJCStorage(client);
        push = initJCPush(client);
        group = initJCGroup(client);
        account = initJCAccount();

        config = JCConfig.create();
        generateDefaultConfig(context);

        client.setDisplayName(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.cloud_setting_key_display_name), ""));
        client.setConfig(JCClient.CONFIG_KEY_SERVER_ADDRESS, PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.cloud_setting_key_server), ""));
        call.maxCallNum = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.cloud_setting_key_call_max_num), ""));
        call.setConference(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.cloud_setting_key_call_audio_conference), false));
        mediaChannel.setConfig(JCMediaChannel.CONFIG_CAPACITY, PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.cloud_setting_key_conference_max_num), ""));

        // 本程序设置为固定方向
        mediaDevice.autoRotate = false;
        return true;
    }

    /**
     * 初始化菊风云客户端
     */
    private JCClient initJCClient(Context context, String appKey) {
        return JCClient.create(context, appKey, new JCClientCallback() {

            @Override
            public void onLogin(boolean result, int reason) {
                EventBus.getDefault().post(new JCLoginEvent(result, reason));

                //处理回调
                if (mOnLoginListeners != null) {
                    for (int i = mOnLoginListeners.size() - 1; i >= 0; i--) {
                        OnLoginListener listener = mOnLoginListeners.get(i);

                        if (listener != null) {
                            if (result) {
                                listener.onSuccess(StringUtil.getString(R.string.jc_login_client_success));

                            } else {
                                listener.onFailure(StringUtil.getString(R.string.jc_login_client_failure));
                            }
                        }

                        mOnLoginListeners.remove(i);
                    }
                }
            }

            @Override
            public void onLogout(int reason) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.LOGOUT));

                //清除数据
                JCMessageData.clear();
                JCGroupData.clear();

                //处理回调
                if (mOnLogoutListeners != null) {
                    for (int i = mOnLogoutListeners.size() - 1; i >= 0; i--) {
                        OnLogoutListener listener = mOnLogoutListeners.get(i);

                        if (listener != null) {
                            listener.onSuccess(StringUtil.getString(R.string.jc_logout_client_success));
                        }

                        mOnLogoutListeners.remove(i);
                    }
                }
            }

            @Override
            public void onClientStateChange(int state, int oldState) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CLIENT_STATE_CHANGE));
            }

        }, null);
    }

    private JCMediaDevice initJCMediaDevice(JCClient client) {
        return JCMediaDevice.create(client, new JCMediaDeviceCallback() {

            @Override
            public void onCameraUpdate() {

            }

            @Override
            public void onAudioOutputTypeChange(boolean speaker) {

            }

            @Override
            public void onRenderReceived(JCMediaDeviceVideoCanvas jcMediaDeviceVideoCanvas) {

            }

            @Override
            public void onRenderStart(JCMediaDeviceVideoCanvas jcMediaDeviceVideoCanvas) {

            }

        });
    }

    private JCMediaChannel initJCMediaChannel(JCClient client, JCMediaDevice mediaDevice) {
        return JCMediaChannel.create(client, mediaDevice, new JCMediaChannelCallback() {

            @Override
            public void onMediaChannelStateChange(int state, int oldState) {

            }

            @Override
            public void onMediaChannelPropertyChange(JCMediaChannel.PropChangeParam propChangeParam) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CONFERENCE_PROP_CHANGE));
            }

            @Override
            public void onJoin(boolean result, int reason, String channelId) {
                EventBus.getDefault().post(new JCJoinEvent(result, reason, channelId));

                //处理回调
                if (mOnJoinChannelListeners != null) {
                    for (int i = mOnJoinChannelListeners.size() - 1; i >= 0; i--) {
                        OnJoinChannelListener listener = mOnJoinChannelListeners.get(i);

                        if (listener != null) {
                            if (result) {
                                listener.onSuccess(StringUtil.getString(R.string.jc_join_channel_success));

                            } else {
                                listener.onFailure(StringUtil.getString(R.string.jc_join_channel_failure));
                            }
                        }

                        mOnJoinChannelListeners.remove(i);
                    }
                }

                if (result && PSTN_MODE) {
                    if (mediaChannel.inviteSipUser(channelId, null) == -1) {
                        mediaChannel.leave();
                    }
                }
            }

            @Override
            public void onLeave(int reason, String channelId) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CONFERENCE_LEAVE));

                //处理回调
                if (mOnLeaveChannelListeners != null) {
                    for (int i = mOnLeaveChannelListeners.size() - 1; i >= 0; i--) {
                        OnLeaveChannelListener listener = mOnLeaveChannelListeners.get(i);

                        if (listener != null) {
                            listener.onSuccess(StringUtil.getString(R.string.jc_leave_channel_success));
                        }

                        mOnLeaveChannelListeners.remove(i);
                    }
                }
            }

            @Override
            public void onStop(boolean result, int reason) {
                EventBus.getDefault().post(new JCConfStopEvent(result, reason));

                //处理回调
                if (mOnStopChannelListeners != null) {
                    for (int i = mOnStopChannelListeners.size() - 1; i >= 0; i--) {
                        OnStopChannelListener listener = mOnStopChannelListeners.get(i);

                        if (listener != null) {
                            if (result) {
                                listener.onSuccess(StringUtil.getString(R.string.jc_stop_channel_success));

                            } else {
                                listener.onFailure(StringUtil.getString(R.string.jc_stop_channel_failure));
                            }
                        }

                        mOnStopChannelListeners.remove(i);
                    }
                }
            }

            @Override
            public void onQuery(int operationId, boolean result, int reason, JCMediaChannelQueryInfo queryInfo) {
                EventBus.getDefault().post(new JCConfQueryEvent(operationId, result, reason, queryInfo));

                //处理回调
                if (mOnQueryChannelListeners != null) {
                    for (int i = mOnQueryChannelListeners.size() - 1; i >= 0; i--) {
                        OnQueryChannelListener listener = mOnQueryChannelListeners.get(i);

                        if (listener != null) {
                            if (result) {
                                listener.onSuccess(StringUtil.getString(R.string.jc_query_channel_success), queryInfo);

                            } else {
                                listener.onFailure(StringUtil.getString(R.string.jc_query_channel_failure));
                            }
                        }

                        mOnQueryChannelListeners.remove(i);
                    }
                }
            }

            @Override
            public void onParticipantJoin(JCMediaChannelParticipant participant) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CONFERENCE_PARTP_JOIN));

                if (PSTN_MODE) {
                    mediaChannel.enableAudioOutput(true);
                }
            }

            @Override
            public void onParticipantLeft(JCMediaChannelParticipant participant) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CONFERENCE_PARTP_LEAVE));

                if (PSTN_MODE) {
                    mediaChannel.leave();
                }
            }

            @Override
            public void onParticipantUpdate(JCMediaChannelParticipant participant, JCMediaChannelParticipant.ChangeParam changeParam) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CONFERENCE_PARTP_UPDATE));
            }

            @Override
            public void onMessageReceive(String type, String content, String fromUserId) {
                EventBus.getDefault().post(new JCConfMessageEvent(type, content, fromUserId));
            }

            @Override
            public void onInviteSipUserResult(int operationId, boolean result, int reason) {
                if (PSTN_MODE && !result) {
                    mediaChannel.leave();
                }
            }

        });
    }

    private JCCall initJCCall(JCClient client, JCMediaDevice mediaDevice) {
        return JCCall.create(client, mediaDevice, new JCCallCallback() {

            @Override
            public void onCallItemAdd(JCCallItem callItem) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_ADD));
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_UI));
            }

            @Override
            public void onCallItemRemove(JCCallItem callItem, int reason, String description) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_REMOVE));
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_UI));
            }

            @Override
            public void onCallItemUpdate(JCCallItem callItem, JCCallItem.ChangeParam changeParam) {
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_UPDATE));
                EventBus.getDefault().post(new JCEvent(JCEvent.EventType.CALL_UI));
            }

            @Override
            public void onMessageReceive(String type, String content, JCCallItem callItem) {
                EventBus.getDefault().post(new JCCallMessageEvent(type, content, callItem));
            }

        });
    }

    private JCMessageChannel initJCMessageChannel(JCClient client) {
        return JCMessageChannel.create(client, new JCMessageChannelCallback() {

            @Override
            public void onMessageSendUpdate(JCMessageChannelItem jcMessageChannelItem) {
                EventBus.getDefault().post(new JCMessageEvent(true, jcMessageChannelItem));
            }

            @Override
            public void onMessageRecv(JCMessageChannelItem jcMessageChannelItem) {
                EventBus.getDefault().post(new JCMessageEvent(false, jcMessageChannelItem));
            }

        });
    }

    private JCStorage initJCStorage(JCClient client) {
        return JCStorage.create(client, new JCStorageCallback() {

            @Override
            public void onFileUpdate(JCStorageItem storageItem) {
                EventBus.getDefault().post(new JCStorageEvent(storageItem));
            }

            @Override
            public void onFileResult(JCStorageItem storageItem) {
                EventBus.getDefault().post(new JCStorageEvent(storageItem));
            }

        });
    }

    private JCPush initJCPush(JCClient client) {
        return JCPush.create(client);
    }

    private JCGroup initJCGroup(JCClient client) {
        return JCGroup.create(client, new JCGroupCallback() {

            @Override
            public void onFetchGroups(int operationId, boolean result, int reason, List<JCGroupItem> groups, long updateTime, boolean fullUpdated) {
                if (result) {
                    JCGroupData.gourpListUpdateTime = updateTime;
                    // 演示群列表更新操作，demo是存入内存，实际应同步到数据库
                    for (JCGroupItem item : groups) {
                        if (item.changeState == JCGroup.GROUP_CHANGE_STATE_ADD) {
                            boolean find = false;
                            for (JCGroupItem temp : JCGroupData.listGroups) {
                                if (TextUtils.equals(temp.groupId, item.groupId)) {
                                    find = true;
                                    break;
                                }
                            }
                            if (!find) {
                                JCGroupData.listGroups.add(0, item);
                            }
                            // 添加群组则去拉下详情
                            group.fetchGroupInfo(item.groupId, JCGroupData.getFetchGroupInfoLastTime(item.groupId));
                        } else if (item.changeState == JCGroup.GROUP_CHANGE_STATE_UPDATE) {
                            for (JCGroupItem temp : JCGroupData.listGroups) {
                                if (TextUtils.equals(temp.groupId, item.groupId)) {
                                    JCGroupData.listGroups.remove(temp);
                                    break;
                                }
                            }
                            JCGroupData.listGroups.add(0, item);
                        } else if (item.changeState == JCGroup.GROUP_CHANGE_STATE_REMOVE) {
                            for (JCGroupItem temp : JCGroupData.listGroups) {
                                if (TextUtils.equals(temp.groupId, item.groupId)) {
                                    // 删除该群组缓存
                                    JCGroupData.mapGroupMembers.remove(item.groupId);
                                    JCGroupData.listGroups.remove(temp);
                                    JCGroupData.mapGroupUpdateTime.remove(item.groupId);
                                    JCMessageData.removeMessages(item.groupId);
                                    break;
                                }
                            }
                        }
                    }
                    EventBus.getDefault().post(new JCEvent(JCEvent.EventType.GROUP_LIST));
                }
            }

            @Override
            public void onFetchGroupInfo(int operationId, boolean result, int reason, JCGroupItem groupItem, List<JCGroupMember> members, long updateTime, boolean fullUpdated) {
                if (result) {
                    // 演示群列表更新操作，demo是存入内存，实际应同步到数据库
                    JCGroupData.setFetchGroupInfoLastTime(groupItem.groupId, updateTime);
                    List<JCGroupMember> saveMembers = null;
                    if (JCGroupData.mapGroupMembers.containsKey(groupItem.groupId)) {
                        saveMembers = JCGroupData.mapGroupMembers.get(groupItem.groupId);
                    } else {
                        saveMembers = new ArrayList<>();
                        JCGroupData.mapGroupMembers.put(groupItem.groupId, saveMembers);
                    }
                    for (JCGroupItem item : JCGroupData.listGroups) {
                        if (TextUtils.equals(item.groupId, groupItem.groupId)) {
                            JCGroupData.listGroups.remove(item);
                            JCGroupData.listGroups.add(groupItem);
                            break;
                        }
                    }
                    for (JCGroupMember member : members) {
                        if (member.changeState == JCGroup.GROUP_CHANGE_STATE_ADD) {
                            boolean find = false;
                            for (JCGroupMember temp : saveMembers) {
                                if (TextUtils.equals(temp.userId, member.userId)) {
                                    find = true;
                                    break;
                                }
                            }
                            if (!find) {
                                saveMembers.add(member);
                            }
                        } else if (member.changeState == JCGroup.GROUP_CHANGE_STATE_UPDATE) {
                            for (JCGroupMember temp : saveMembers) {
                                if (TextUtils.equals(temp.userId, member.userId)) {
                                    saveMembers.remove(temp);
                                    break;
                                }
                            }
                            saveMembers.add(member);
                        } else if (member.changeState == JCGroup.GROUP_CHANGE_STATE_REMOVE) {
                            for (JCGroupMember temp : saveMembers) {
                                // 删除只能根据uid来进行判断
                                if (TextUtils.equals(temp.uid, member.uid)) {
                                    saveMembers.remove(temp);
                                    break;
                                }
                            }
                        }
                    }
                    EventBus.getDefault().post(new JCEvent(JCEvent.EventType.GROUP_INFO));
                }
            }

            @Override
            public void onGroupListChange() {
                group.fetchGroups(JCGroupData.gourpListUpdateTime);
            }

            @Override
            public void onGroupInfoChange(String groupId) {
                group.fetchGroupInfo(groupId, JCGroupData.getFetchGroupInfoLastTime(groupId));
            }

            @Override
            public void onCreateGroup(int operationId, boolean result, int reason, JCGroupItem groupItem) {
                if (!result) {
                    ToastUtil.show("创建群失败");
                }
            }

            @Override
            public void onUpdateGroup(int operationId, boolean result, int reason, String groupId) {
                group.fetchGroupInfo(groupId, JCGroupData.getFetchGroupInfoLastTime(groupId));
            }

            @Override
            public void onDissolve(int operationId, boolean result, int reason, String groupId) {
            }

            @Override
            public void onLeave(int operationId, boolean result, int reason, String groupId) {
            }

            @Override
            public void onDealMembers(int operationId, boolean result, int reason) {
            }

        });
    }

    private JCAccount initJCAccount() {
        return JCAccount.create((i, result, list) -> {
            JCAccountQueryStatusEvent event = new JCAccountQueryStatusEvent(result, list);
            EventBus.getDefault().post(event);
        });
    }

    public void release() {
        if (client != null) {
            JCPush.destroy();
            JCStorage.destroy();
            JCMessageChannel.destroy();
            JCCall.destroy();
            JCMediaChannel.destroy();
            JCMediaDevice.destroy();
            JCClient.destroy();
            JCAccount.destroy();
            JCConfig.destory();
            push = null;
            storage = null;
            messageChannel = null;
            call = null;
            mediaChannel = null;
            mediaDevice = null;
            client = null;
            account = null;
            config = null;
        }
    }

    /**
     * 登录菊风云
     *
     * @param jcUserId 菊风云的用户名
     * @param jcPwd    菊风云的登录密码
     */
    public void login(String jcUserId, String jcPwd, OnLoginListener listener) {
        if (listener != null) {
            mOnLoginListeners.add(listener);
            listener.onStart();
        }

        if (!client.login(jcUserId, jcPwd)) {
            if (listener != null) {
                listener.onFailure(StringUtil.getString(R.string.jc_login_client_failure));
                mOnLoginListeners.remove(listener);
            }
        }
    }

    /**
     * 退出菊风云
     */
    public void logout(OnLogoutListener listener) {
        if (listener != null) {
            mOnLogoutListeners.add(listener);
            listener.onStart();
        }

        if (!client.logout()) {
            if (listener != null) {
                listener.onFailure(StringUtil.getString(R.string.jc_logout_client_failure));
                mOnLogoutListeners.remove(listener);
            }
        }
    }

    /**
     * 查询会议
     *
     * @param channelId 频道id
     */
    public void queryChannel(String channelId, OnQueryChannelListener listener) {
        if (listener != null) {
            mOnQueryChannelListeners.add(listener);
            listener.onStart();
        }

        mediaChannel.query(channelId);
    }

    /**
     * 加入问诊
     *
     * @param channelId  频道id
     * @param channelPwd 频道密码
     */
    public void joinChannel(String channelId, String channelPwd, OnJoinChannelListener listener) {
        if (listener != null) {
            mOnJoinChannelListeners.add(listener);
            listener.onStart();
        }

        Map<String, String> param = new HashMap<>();
        if (!TextUtils.isEmpty(channelPwd)) {
            param.put(JCMediaChannel.JOIN_PARAM_PASSWORD, channelPwd);
        }
        param.put(JCMediaChannel.JOIN_PARAM_SMOOTH_MODE, Boolean.toString(true));
        mediaChannel.enableUploadAudioStream(true);
        mediaChannel.enableUploadVideoStream(true);
        param.put(JCMediaChannel.JOIN_PARAM_MAX_RESOLUTION, "0");
        param.put(JCMediaChannel.JOIN_PARAM_URI_MODE, Boolean.toString(false));

        if (!mediaChannel.join(channelId, param)) {
            if (listener != null) {
                listener.onFailure(StringUtil.getString(R.string.jc_join_channel_failure));
                mOnJoinChannelListeners.remove(listener);
            }
        }
    }

    /**
     * 离开问诊
     */
    public void leaveChannel(OnLeaveChannelListener listener) {
        if (listener != null) {
            mOnLeaveChannelListeners.add(listener);
            listener.onStart();
        }

        if (!mediaChannel.leave()) {
            if (listener != null) {
                listener.onFailure(StringUtil.getString(R.string.jc_leave_channel_failure));
                mOnLeaveChannelListeners.remove(listener);
            }
        }
    }

    /**
     * 结束问诊
     */
    public void stopChannel(OnStopChannelListener listener) {
        if (listener != null) {
            mOnStopChannelListeners.add(listener);
            listener.onStart();
        }

        if (!mediaChannel.stop()) {
            if (listener != null) {
                listener.onFailure(StringUtil.getString(R.string.jc_stop_channel_failure));
                mOnStopChannelListeners.remove(listener);
            }
        }
    }

    // 生成默认配置
    private void generateDefaultConfig(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String value = sp.getString(context.getString(R.string.cloud_setting_key_display_name), "");
        if (TextUtils.isEmpty(value)) {
            editor.putString(context.getString(R.string.cloud_setting_key_display_name), "");
        }
        value = sp.getString(context.getString(R.string.cloud_setting_key_server), "");
        if (TextUtils.isEmpty(value)) {
            editor.putString(context.getString(R.string.cloud_setting_key_server), client.getConfig(JCClient.CONFIG_KEY_SERVER_ADDRESS));
        }
        value = sp.getString(context.getString(R.string.cloud_setting_key_call_max_num), "");
        if (TextUtils.isEmpty(value)) {
            editor.putString(context.getString(R.string.cloud_setting_key_call_max_num), String.valueOf(call.maxCallNum));
        }
        value = sp.getString(context.getString(R.string.cloud_setting_key_conference_max_num), "");
        if (TextUtils.isEmpty(value)) {
            editor.putString(context.getString(R.string.cloud_setting_key_conference_max_num), mediaChannel.getConfig(JCMediaChannel.CONFIG_CAPACITY));
        }
        editor.apply();
    }

    private static final class JCManagerHolder {
        private static final JCManager INSTANCE = new JCManager();
    }

}
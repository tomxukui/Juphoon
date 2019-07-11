package com.xukui.juphoon.permission;

import android.content.Context;
import android.support.annotation.Nullable;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtil {

    /**
     * 请求权限
     *
     * @param context     上下文
     * @param options     权限对象
     * @param granted     成功回调
     * @param denied      失败回调
     * @param permissions 权限集合
     */
    public static void requestPermission(final Context context, Options options, final Action<List<String>> granted, @Nullable final Action<List<String>> denied, final String... permissions) {
        if (AndPermission.hasPermissions(context, permissions)) {
            if (granted != null) {
                granted.onAction(Arrays.asList(permissions));
            }

        } else {
            options.runtime()
                    .permission(permissions)
                    .rationale(new RuntimeRationale())
                    .onGranted(granted)
                    .onDenied(data -> {
                        if (AndPermission.hasPermissions(context, permissions)) {
                            if (granted != null) {
                                granted.onAction(data);
                            }

                        } else {
                            if (denied != null) {
                                denied.onAction(data);
                            }
                        }
                    })
                    .start();
        }
    }

    /**
     * 请求权限
     *
     * @param context 上下文
     * @param options 权限对象
     * @param granted 成功回调
     * @param denied  失败回调
     * @param groups  权限组集合
     */
    public static void requestPermission(final Context context, Options options, final Action<List<String>> granted, @Nullable final Action<List<String>> denied, final String[]... groups) {
        if (AndPermission.hasPermissions(context, groups)) {
            if (granted != null) {
                granted.onAction(groups2List(groups));
            }

        } else {
            options.runtime()
                    .permission(groups)
                    .rationale(new RuntimeRationale())
                    .onGranted(granted)
                    .onDenied(data -> {
                        if (AndPermission.hasPermissions(context, groups)) {
                            if (granted != null) {
                                granted.onAction(data);
                            }

                        } else {
                            if (denied != null) {
                                denied.onAction(data);
                            }
                        }
                    })
                    .start();
        }
    }

    private static List<String> groups2List(String[]... groups) {
        List<String> list = new ArrayList<>();

        if (groups != null) {
            for (int i = 0; i < groups.length; i++) {
                String[] items = groups[i];

                if (items != null) {
                    list.addAll(Arrays.asList(items));
                }
            }
        }

        return list;
    }

}
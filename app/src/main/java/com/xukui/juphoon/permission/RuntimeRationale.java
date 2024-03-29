package com.xukui.juphoon.permission;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;

import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

public final class RuntimeRationale implements Rationale<List<String>> {

    @Override
    public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = String.format("允许以下权限以便程序继续执行：\n\n%1$s", TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("继续", (dialog, which) -> executor.execute())
                .setNegativeButton("取消", (dialog, which) -> executor.cancel())
                .show();
    }

}
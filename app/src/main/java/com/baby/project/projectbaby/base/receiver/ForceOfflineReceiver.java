package com.baby.project.projectbaby.base.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.App;
import com.baby.project.projectbaby.base.util.ActivityCollector;
import com.baby.project.projectbaby.sign.in.SignInActivity;
import com.orhanobut.logger.Logger;

import java.util.Objects;

/**
 * 下线广播，用于广播强制下线消息
 */
public class ForceOfflineReceiver extends BroadcastReceiver {

    private static final String KEY_FORCE_OFFLINE_USER_OBJECT_ID = "key:force_offline_user_object_id";

    public static Intent getIntent(String userObjectId) {
        Intent intent = new Intent();
        intent.putExtra(KEY_FORCE_OFFLINE_USER_OBJECT_ID, userObjectId);
        intent.setAction(ReceiverActionContract.FORCE_OFFLINE_RECEIVER_ACTION);
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ReceiverActionContract.FORCE_OFFLINE_RECEIVER_ACTION.equals(intent.getAction())) {
            // 因为只有目标设备能接收到推送，所以不用判断installation id
            // 需要判断当前用户是否是需要下线的用户
            String forceOfflineUserId = intent.getStringExtra(KEY_FORCE_OFFLINE_USER_OBJECT_ID);
            if (forceOfflineUserId == null) {
                return;
            }
            AVUser currentUser = AVUser.getCurrentUser();
            if (currentUser == null || !currentUser.getObjectId().equals(forceOfflineUserId)) {
                Logger.d(currentUser.getObjectId() + "current user will not login or force offline user not equal to current user,should not force offline.");
                return;
            }
            // TODO force offline and clear cache and data
            ActivityCollector.finishAll();
            SignInActivity.startCauseForceOffline(context);
//        if (Build.VERSION.SDK_INT >= 23) {
            // 是否有悬浮球权限
//            if(!Settings.canDrawOverlays(context)) {
//                Intent d = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
//                context.startActivity(d);
//            }else {
//                AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Dialog_Alert)
//                        .setTitle(R.string.hint_text)
//                        .setMessage(R.string.force_offline_content_text)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.confrim_text, (dialog, which) -> {
//                            dialog.dismiss();
//                            // logout uer and clear cache
//                            AVUser.logOut();
//                            // exit app
//                            ActivityAndEventBusCollector.finishAll();
//                            Intent mainIntent = new Intent();
//                            mainIntent.setClass(context, MainActivity.class);
//                            // Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag
//                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(mainIntent);
//                        })
//                        .create();
//                if (Build.VERSION.SDK_INT >= 26) {
//                    // 在非activity弹窗 参见https://www.zhihu.com/question/37849134
//                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//                }else {
//                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                }
//                alertDialog.show();
//            }
//        }else {
//            AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Dialog_Alert)
//                    .setTitle(R.string.hint_text)
//                    .setMessage(R.string.force_offline_content_text)
//                    .setCancelable(false)
//                    .setPositiveButton(R.string.confrim_text, (dialog, which) -> {
//                        dialog.dismiss();
//                        // logout uer and clear cache
//                        AVUser.logOut();
//                        // exit app
//                        ActivityAndEventBusCollector.finishAll();
//                        Intent mainIntent = new Intent();
//                        mainIntent.setClass(context, MainActivity.class);
//                        // Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag
//                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        context.startActivity(mainIntent);
//                    })
//                    .create();
//            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//            alertDialog.show();
//        }
        }
    }
}

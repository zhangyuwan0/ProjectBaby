package com.baby.project.projectbaby.base.receiver;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.App;
import com.baby.project.projectbaby.base.ProjectBabyConfig;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

public class CloudPushReceiver extends BroadcastReceiver {

    // 强制下线action
    public static final String ACTION_FORCE_OFFLINE = ReceiverActionContract.FORCE_OFFLINE_RECEIVER_ACTION;

    // 已经在android manifest中注册action了 所以这里不用判断action
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("onReceive");
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString(ProjectBabyConfig.DATA_TAG_PUSH_DATA));
            String action = json.getString("action");
            if (TextUtils.isEmpty(action)) {
                Logger.e("push message has not action tag,please check code and retry.");
                Logger.json(json.toString());
                return;
            }

            switch (action) {
                case ACTION_FORCE_OFFLINE:
                    LocalBroadcastManager.getInstance(context).sendBroadcast(ForceOfflineReceiver.getIntent(json.getString("force_offline_user_id")));
                    break;
                //TODO 处理其他push信息 最后应该弄个controller同一dispatch
                default:
                    final String message = json.getString("alert");
                    Intent resultIntent = new Intent(AVOSCloud.applicationContext, MainActivity.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(AVOSCloud.applicationContext, 0, resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(AVOSCloud.applicationContext, App.CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_launcher_round)
                                    .setContentTitle(
                                            AVOSCloud.applicationContext.getResources().getString(R.string.app_name))
                                    .setContentText(message)
                                    .setTicker(message);
                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setAutoCancel(true);

                    int mNotificationId = 10086;
                    NotificationManager mNotifyMgr =
                            (NotificationManager) AVOSCloud.applicationContext
                                    .getSystemService(
                                            Context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }

        } catch (Exception e) {
            Logger.e(e, e.getMessage());
        }
    }
}

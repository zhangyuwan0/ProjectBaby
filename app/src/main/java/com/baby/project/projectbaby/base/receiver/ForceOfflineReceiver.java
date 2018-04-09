package com.baby.project.projectbaby.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.util.ActivityAndEventBusCollector;

/**
 * 下线广播，用于广播强制下线消息
 */
public class ForceOfflineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlertDialog alertDialog = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle(R.string.hint_text)
                .setMessage(R.string.force_offline_content_text)
                .setCancelable(false)
                .setPositiveButton(R.string.confrim_text, (dialog, which) -> {
                    dialog.dismiss();
                    // logout uer and clear cache
                    AVUser.logOut();
                    // exit app
                    ActivityAndEventBusCollector.finishAll();
                    Intent mainIntent = new Intent();
                    mainIntent.setClass(context, MainActivity.class);
                    // Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mainIntent);
                })
                .create();
        // 在非activity弹窗 参见https://www.zhihu.com/question/37849134
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}

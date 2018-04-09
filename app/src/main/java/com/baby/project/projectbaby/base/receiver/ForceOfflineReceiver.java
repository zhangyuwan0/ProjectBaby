package com.baby.project.projectbaby.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.util.ActivityAndEventBusCollector;

/**
 * 下线广播，用于广播强制下线消息
 */
public class ForceOfflineReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.hint_text)
                .setMessage(R.string.force_offline_content_text)
                .setNegativeButton(R.string.confrim_text, (dialog, which) -> {
                    // logout uer and clear cache
                    AVUser.logOut();
                    // exit app
                    ActivityAndEventBusCollector.finishAll();
                    dialog.dismiss();
                    Intent mainIntent = new Intent();
                    mainIntent.setClass(context, MainActivity.class);
                    context.startActivity(mainIntent);
                })
                .create()
                .show();
    }
}

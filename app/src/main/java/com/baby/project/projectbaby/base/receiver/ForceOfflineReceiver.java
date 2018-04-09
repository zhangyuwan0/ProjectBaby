package com.baby.project.projectbaby.base.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.base.event.ForceOfflineEvent;
import com.baby.project.projectbaby.base.util.ActivityAndEventBusCollector;

import org.greenrobot.eventbus.EventBus;

/**
 * 下线广播，用于广播强制下线消息
 */
public class ForceOfflineReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO 其他清理操作
        EventBus.getDefault().post(new ForceOfflineEvent());
    }
}

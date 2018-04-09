package com.baby.project.projectbaby.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.baby.project.projectbaby.MainActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.event.ForceOfflineEvent;
import com.baby.project.projectbaby.base.receiver.ForceOfflineReceiver;
import com.baby.project.projectbaby.base.receiver.ReceiverActionContract;
import com.baby.project.projectbaby.base.util.ActivityAndEventBusCollector;
import com.project.baby.clouddialog.CloudDialog;
import com.project.baby.clouddialog.CloudDialogListeners;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Activity基类
 * Created by yosemite on 2018/3/29.
 */

public class BaseActivity extends AppCompatActivity {

    private ForceOfflineReceiver mForceOfflineReceiver;
    protected LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        // 注册activity收集器
        ActivityAndEventBusCollector.register(this);
        // 本地广播 send force offline broadcast receiver
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity在栈顶，注册强制下线广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverActionContract.FORCE_OFFLINE_REIVER_ACTION);
        mForceOfflineReceiver = new ForceOfflineReceiver();
        mLocalBroadcastManager.registerReceiver(mForceOfflineReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity不在栈顶，取消注册强制下线广播接收器
        if (mForceOfflineReceiver != null) {
            mLocalBroadcastManager.unregisterReceiver(mForceOfflineReceiver);
            mForceOfflineReceiver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ForceOfflineEvent event) {
        new CloudDialog.Builder(this, getSupportFragmentManager())
                .isCustomDialog(false)
                .setCancelable(false)
                .setDialog(new CloudDialogListeners.OnCallDialogListener() {
                    @Override
                    public Dialog getDialog(Bundle savedInstanceState) {
                        return new AlertDialog.Builder(getBaseContext())
                                .setTitle(R.string.hint_text)
                                .setMessage(R.string.force_offline_content_text)
                                .setNegativeButton(R.string.confrim_text, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent mainIntent = new Intent();
                                        mainIntent.setClass(BaseActivity.this,MainActivity.class);
                                        BaseActivity.this.startActivity(mainIntent);
                                    }
                                })
                                .create();
                    }
                }).show();
    }

}

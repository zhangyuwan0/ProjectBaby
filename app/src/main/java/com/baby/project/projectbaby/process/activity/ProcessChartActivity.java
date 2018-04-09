package com.baby.project.projectbaby.process.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.baby.project.projectbaby.base.BaseActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.receiver.ReceiverActionContract;

/**
 * 工程进度图activity
 * Created by yosemite on 2018/3/29.
 */

public class ProcessChartActivity extends BaseActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set it to be no title*/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_process_chart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent();
        intent.setAction(ReceiverActionContract.FROCE_OFFLINE_REIVER_ACTION);
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}

package com.baby.project.projectbaby.process.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;

import com.baby.project.projectbaby.BaseActivity;
import com.baby.project.projectbaby.R;

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
}

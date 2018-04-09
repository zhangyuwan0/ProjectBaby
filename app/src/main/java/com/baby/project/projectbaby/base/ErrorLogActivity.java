package com.baby.project.projectbaby.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.baby.project.projectbaby.R;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

public class ErrorLogActivity extends Activity {
    private static final String TAG = "ErrorLogActivity";

    public static final String KEY_STACK_LOG = "key:stackLog";
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_log);
        TextView tvErrorLog = findViewById(R.id.tv_error_log);
        initContent();
        tvErrorLog.setText(content);
        // 保存log信息到磁盘
        FormatStrategy formatStrategy = CsvFormatStrategy.newBuilder()
                .tag(TAG)
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(formatStrategy));
        Logger.e(content);
        Logger.clearLogAdapters();
    }


    private void initContent() {
        Intent i = getIntent();
        if (i.hasExtra(KEY_STACK_LOG)) {
            content = i.getExtras().getString(KEY_STACK_LOG);
        } else {
            finish();
        }
    }
}

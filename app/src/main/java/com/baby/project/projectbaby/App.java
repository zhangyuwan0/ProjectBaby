package com.baby.project.projectbaby;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;

/**
 * app
 * Created by yosemite on 2018/3/20.
 */

public class App extends Application {

    private static final String APP_ID = "griPia4pQjpE141XAPML6fio-gzGzoHsz";
    private static final String APP_KEY = "y8BRkphSQhDVhALOrgzf0vvk";
    public static final String CHANNEL_ID = "project_baby_push_channel_id";

    private static final String TAG = "ProjectBabyApplication";

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {
        super.onCreate();
        PushService.setDefaultChannelId(this, CHANNEL_ID);
        // 批量注册lean cloud 的子类化
        InitializeUtil.registerSubClasses();
        AVOSCloud.initialize(this,APP_ID,APP_KEY);
        // 启用崩溃错误统计
        AVAnalytics.enableCrashReport(this.getApplicationContext(), true);
        // 开启错误调试日志
        // 放在 SDK 初始化语句 AVOSCloud.initialize() 后面，只需要调用一次即可
        AVOSCloud.setDebugLogEnabled(true);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

    }

}

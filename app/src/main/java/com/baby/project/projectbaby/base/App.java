package com.baby.project.projectbaby.base;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVLogger;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.baby.project.projectbaby.sign.SplashActivity;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * app
 * Created by yosemite on 2018/3/20.
 */

public class App extends Application implements Thread.UncaughtExceptionHandler {

    private static final String APP_ID = "griPia4pQjpE141XAPML6fio-gzGzoHsz";
    private static final String APP_KEY = "y8BRkphSQhDVhALOrgzf0vvk";
    public static final String CHANNEL_ID = "project_baby_push_channel_id";

    private static final String TAG = "ProjectBabyApplication";

    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化Logger
        Logger.addLogAdapter(new AndroidLogAdapter() {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return ProjectBabyConfig.isDeveloperMode() || ProjectBabyConfig.isTesterMode();
            }
        });
        // 捕获异常
        Thread.setDefaultUncaughtExceptionHandler(this);
        PushService.setDefaultChannelId(this, CHANNEL_ID);
        // 初始化leanCloud
        AVOSCloud.initialize(this, APP_ID, APP_KEY);
        // 批量注册lean cloud 的子类化
        InitializeUtil.registerSubClasses();
        // 保存installation信息
        AVInstallation.getCurrentInstallation().saveInBackground();
        // 开启待机时推送自动唤醒
        PushService.setAutoWakeUp(true);
        // 启动推送
        PushService.setDefaultPushCallback(this, SplashActivity.class);
        // 启用崩溃错误统计
        AVAnalytics.enableCrashReport(this.getApplicationContext(), true);
        // 开启错误调试日志
        // 放在 SDK 初始化语句 AVOSCloud.initialize() 后面，只需要调用一次即可
        AVOSCloud.setDebugLogEnabled(true);

    }

    // -------------------异常捕获事件 + ---------------------------
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (ProjectBabyConfig.isTesterMode() || ProjectBabyConfig.isDeveloperMode()) {
            handleException(ex);
            showStackLog(ex);
        } else {
            handleException(ex);
            restartAPP(ex);
            // 保存日志文件
            String fileName = saveCrashInfo2File(ex);

        }
        System.exit(0);
        System.gc();
    }

    // ---------------------启动错误调试日志-----------------------------
    private void showStackLog(Throwable ex) {
        Intent intent = new Intent(getApplicationContext(), ErrorLogActivity.class);
        intent.putExtra(ErrorLogActivity.KEY_STACK_LOG, getErrorLog(ex));
        PendingIntent restartIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                restartIntent);
    }

    // ---------------------重启APP-----------------------------
    private void restartAPP(Throwable ex) {
        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.putExtra(SplashActivity.KEY_ERROR_LOG, getErrorLog(ex));
        intent.putExtra(SplashActivity.KEY_ERROR_LOG_FILE, saveCrashInfo2File(ex));
        PendingIntent restartIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                restartIntent);
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex the error object
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 收集设备参数信息
        collectDeviceInfo(getApplicationContext());
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx object
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Logger.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Logger.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    private String getErrorLog(Throwable ex) {
        return getDeviceLog() + getStackErrorLog(ex);
    }

    private String getDeviceLog() {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }
        return sb.toString();
    }

    // -------------------异常捕获事件 - ---------------------------

    // --------------------模板方法-----------------

    private String getStackErrorLog(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString();
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
        private String saveCrashInfo2File(Throwable ex) {

        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".txt";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {

                File fileDir = Environment.getExternalStorageDirectory();
                String path = fileDir.getPath() + "/project_baby/log";

                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + "/" + fileName);
                fos.write(getErrorLog(ex).getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Logger.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }
}

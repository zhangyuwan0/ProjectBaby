package com.baby.project.projectbaby.sign;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.BaseActivity;
import com.orhanobut.logger.Logger;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 闪页
 * Created by yosemite on 2018/4/9.
 */

public class SplashActivity extends BaseActivity {

    public static final String KEY_ERROR_LOG = "key:error_log";
    public static final String KEY_ERROR_LOG_FILE = "key:error_log";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        /*set it to be no title*/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handleErrorLog();
        setContentView(R.layout.activity_main);
    }


    @SuppressLint("CheckResult")
    private void handleErrorLog() {
        Observable.just(getIntent().getStringExtra(KEY_ERROR_LOG))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .compose(this.bindToLifecycle())
                .flatMap((Function<String, ObservableSource<AVFile>>) errorLogContent -> {
                    final String filename = "crash-" + new Date().toString() + "-" + System.currentTimeMillis() + ".txt";
                    final AVFile avFile = new AVFile(filename, errorLogContent.getBytes());
                    return Observable.create(emitter -> avFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null) {
                                emitter.onNext(avFile);
                            } else {
                                emitter.onError(e);
                            }
                        }
                    }));
                })
                .subscribe(avFile -> {
                    AVObject errorLog = new AVObject("_error_log");
                    errorLog.put("fileName", avFile.getOriginalName());
                    errorLog.put("fileId", avFile.getObjectId());
                    errorLog.put("fileUrl", avFile.getUrl());
                }, throwable -> Logger.e(throwable.getMessage()));

    }


}

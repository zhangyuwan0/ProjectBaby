package com.baby.project.projectbaby.sign;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.BaseActivity;
import com.baby.project.projectbaby.sign.in.presenter.SignInPresenter;
import com.baby.project.projectbaby.sign.in.view.SignInView;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 闪页
 * auto login时
 * 应该检查当前用户installation表中的信息与现在是否一致 不一致需要重新登录
 * 还应检查当前用户的updateTime 超过7天为过期 需重新登录？
 * Created by yosemite on 2018/4/9.
 */

public class SplashActivity extends BaseActivity<SignInView, SignInPresenter> {

    public static final String KEY_ERROR_LOG = "key:error_log";

    @Override
    protected SignInPresenter createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set it to be no title*/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        handleErrorLog();
    }

    private void handleErrorLog() {
        if (!getIntent().hasExtra(KEY_ERROR_LOG)){
           return;
        }

        Observable.just(getIntent().getStringExtra(KEY_ERROR_LOG))
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap((Function<String, ObservableSource<AVFile>>) errorLogContent -> {
                    long timestamp = System.currentTimeMillis();
                    String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                    String fileName = "crash-" + time + "-" + timestamp + ".txt";
                    final AVFile avFile = new AVFile(fileName, errorLogContent.getBytes());
                    return Observable.create(emitter -> avFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                emitter.onNext(avFile);
                            } else {
                                emitter.onError(e);
                            }
                        }
                    }));
                })
                .as(bindLifecycle())
                .subscribe(avFile -> {
                    AVObject errorLog = new AVObject("error_log");
                    errorLog.put("fileName", avFile.getOriginalName());
                    errorLog.put("fileId", avFile.getObjectId());
                    errorLog.put("fileUrl", avFile.getUrl());
                    errorLog.saveInBackground();
                }, throwable -> Logger.e(throwable.getMessage()));
    }


}

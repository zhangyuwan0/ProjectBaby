package com.baby.project.projectbaby.process.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.Toast;

import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.baby.project.projectbaby.base.BaseActivity;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.sign.in.presenter.SignInPresenter;
import com.baby.project.projectbaby.sign.in.view.SignInView;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 工程进度图activity
 * Created by yosemite on 2018/3/29.
 */

public class ProcessChartActivity extends BaseActivity<SignInView,SignInPresenter> {

    @Override
    protected SignInPresenter createPresenter() {
        return null;
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set it to be no title*/
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_process_chart);
        Observable.create((ObservableOnSubscribe<HashMap<String,Object>>) emitter -> {
            HashMap<String,Object> loginParams = new HashMap<>();
            loginParams.put("phoneNumber", "15147053503");
            loginParams.put("password", "123456");
            // TODO 要保证Installation信息保存成功
            loginParams.put("installationId", AVInstallation.getCurrentInstallation().getInstallationId());
            emitter.onNext(AVCloud.callFunction("loginUseMobilePhoneNumberAndPassword", loginParams));
        }).observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map(new Function<HashMap<String,Object>, AVUser>() {

            @Override
            public AVUser apply(HashMap<String,Object> result) throws Exception {
                AVUser avUser = new AVUser();
                // 转换结果集
                AVUtils.copyPropertiesFromMapToAVObject(result,avUser);
                // 保存当前用户
                AVUser.changeCurrentUser(avUser, true);
                return avUser;
            }
        }).map(new Function<AVUser, AVUser>() {
            @Override
            public AVUser apply(AVUser avUser) throws Exception {
                AVQuery<AVObject> avQuery = new AVQuery<>("error_log");
                avQuery.orderByDescending("createdAt");
                List<AVObject> result = avQuery.find();
                if (result != null) {
                    Logger.e(result.toString());
                }
                return avUser;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .as(bindLifecycle())
                .subscribe(avUser -> {
                    if (avUser != null) {
                        Toast.makeText(getApplicationContext(), "login success" + AVUser.getCurrentUser().getObjectId(), Toast.LENGTH_SHORT).show();

                    }
                }, throwable -> {
                    Logger.e(throwable.getMessage());
                    Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

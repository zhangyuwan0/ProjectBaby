package com.baby.project.projectbaby.base;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.baby.project.projectbaby.base.receiver.ForceOfflineReceiver;
import com.baby.project.projectbaby.base.receiver.ReceiverActionContract;
import com.baby.project.projectbaby.base.util.ActivityCollector;
import com.baby.project.projectbaby.base.util.RxLifecycleUtils;
import com.uber.autodispose.AutoDisposeConverter;

/**
 * Activity基类
 * Created by yosemite on 2018/3/29.
 */

public abstract class BaseActivity<V extends IView, P extends IPresenter<V>> extends AppCompatActivity {

    private ForceOfflineReceiver mForceOfflineReceiver;
    protected LocalBroadcastManager mLocalBroadcastManager;

    private P presenter;

    protected abstract P createPresenter();

    protected P getPresenter() {
        return this.presenter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册activity收集器
        ActivityCollector.register(this);
        // 本地广播 send force offline broadcast receiver
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
//        this.presenter = createPresenter();
        // 注册presenter为lifecycle observer
//        getLifecycle().addObserver(getPresenter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity在栈顶，注册强制下线广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverActionContract.FORCE_OFFLINE_RECEIVER_ACTION);
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

    protected <T> AutoDisposeConverter<T> bindLifecycle() {
        return RxLifecycleUtils.bindLifecycle(this);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {

            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();

            if (isShouldHideInput(v, ev)) {
                hideSoftInput(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 多种隐藏软件盘方法的其中一种
     *
     * @param token
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}

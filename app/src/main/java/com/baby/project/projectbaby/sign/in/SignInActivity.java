package com.baby.project.projectbaby.sign.in;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.R;
import com.baby.project.projectbaby.base.BaseActivity;
import com.baby.project.projectbaby.sign.in.presenter.SignInPresenter;
import com.baby.project.projectbaby.sign.in.view.SignInView;

/**
 * Created by yosemite on 2018/4/9.
 */

public class SignInActivity extends BaseActivity<SignInView,SignInPresenter> {

    public static final String KEY_ACCOUNT_STATUS_CODE = "key:account_status_code";

    public static final int ACCOUNT_STATUS_CODE_UN_KNOW = -1;
    public static final int ACCOUNT_STATUS_CODE_FORCE_OFFLINE = 0;
    public static final int ACCOUNT_STATUS_CODE_CHANGE_PASSWORD = 1;
    public static final int ACCOUNT_STATUS_CODE_SIGN_OUT = 2;

    public static void startCauseForceOffline(Context context) {
        Intent forceOfflineIntent = new Intent();
        forceOfflineIntent.setClass(context, SignInActivity.class);
        forceOfflineIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        forceOfflineIntent.putExtra(SignInActivity.KEY_ACCOUNT_STATUS_CODE, SignInActivity.ACCOUNT_STATUS_CODE_FORCE_OFFLINE);
        context.startActivity(forceOfflineIntent);
    }

    @Override
    protected SignInPresenter createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleAccountStatusEvent();
    }

    // TODO 交由控制器处理
    private void handleAccountStatusEvent() {
        int accountStatusCode = getIntent().getIntExtra(KEY_ACCOUNT_STATUS_CODE, ACCOUNT_STATUS_CODE_UN_KNOW);
        switch (accountStatusCode) {
            case ACCOUNT_STATUS_CODE_FORCE_OFFLINE:
                showForceOfflineAlertDialog();
                break;
            case ACCOUNT_STATUS_CODE_CHANGE_PASSWORD:
                break;
            case ACCOUNT_STATUS_CODE_SIGN_OUT:
                break;
            case ACCOUNT_STATUS_CODE_UN_KNOW:
            default:
                break;
        }
    }

    private void showForceOfflineAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.hint_text)
                .setMessage(R.string.force_offline_content_text)
                .setCancelable(false)
                .setPositiveButton(R.string.confirm_text, (dialog, which) -> {
                    dialog.dismiss();
                    // TODO 后续处理应交由控制器处理
                    // logout uer and clear cache
                    AVUser.logOut();
                })
                .create()
                .show();
    }


}

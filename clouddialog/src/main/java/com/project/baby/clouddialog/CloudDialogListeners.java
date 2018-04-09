package com.project.baby.clouddialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * CloudDialog 相关监听器
 * Created by yosemite on 2018/2/26.
 */

public class CloudDialogListeners {

    /**
     * 创建dialog的回调监听器，用于指定所要回调的事件
     */
    public interface OnCallDialogListener {
        /**
         * 可以在该函数中，创建要指定的dialog
         *
         * @param savedInstanceState 保存的dialog状态
         * @return 所要指定的dialog
         */
        Dialog getDialog(Bundle savedInstanceState);
    }

    /**
     * CloudDialog生命周期监听器，dialog创建过程的相关Hook函数
     */
    public interface OnLifecycleListener {
        /**
         * 初始化dialog前调用
         * @param savedInstanceState 保存的状态信息
         */
        void onPreInit(Bundle savedInstanceState);

        /**
         * 绑定view时调用
         *
         * @param dialog             {@link DialogFragment#getDialog()}
         * @param contentView        绑定的内容布局，如果指定了dialog则返回null
         * @param savedInstanceState 保存的实例状态，用于恢复数据
         */
        void onCreateDialogView(Dialog dialog, @Nullable View contentView, @Nullable Bundle savedInstanceState);

        /**
         * view绑定完成后调用
         *
         * @param dialog      创建的dialog，如果未通过指定dialog的方式使用，
         *                    则返回{@link DialogFragment#getDialog()}的值
         * @param contentView 绑定的内容布局，如果指定了dialog则返回null
         */
        void onPostInit(Dialog dialog, @Nullable View contentView);

        /**
         * Dialog保存状态所用接口，用于保存：
         * 1.用户自定义的一些参数
         * 2.系统控件或自定义控件无法自动保存的参数
         * 在 {@link CloudDialog#onSaveInstanceState(Bundle)}中调用
         *
         * @param outState 要输出的实例状态
         */
        void onSaveInstanceState(Bundle outState);
    }

    /**
     * 初始化监听器的适配器，可以选择性实现相关回调
     */
    public static class OnLifecycleListenerAdapter implements OnLifecycleListener {

        @Override
        public void onPreInit(Bundle savedInstanceState) {}

        @Override
        public void onCreateDialogView(Dialog dialog, View contentView, Bundle savedInstanceState) {}

        @Override
        public void onPostInit(Dialog dialog, View contentView) {}

        @Override
        public void onSaveInstanceState(Bundle outState) {}
    }

}

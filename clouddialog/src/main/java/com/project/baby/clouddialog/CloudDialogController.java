package com.project.baby.clouddialog;

import android.content.DialogInterface;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * 数据保存封装的容器类
 * 设计类似AlertDialog的AlertController
 * <p>
 * Created by yosemite on 2018/2/26.
 */

public class CloudDialogController {

    private FragmentManager mFragmentManager;
    private View mView; // 最终要显示的View
    @StyleRes
    private int dialogThemeId; // dialog的theme
    private int style = DialogFragment.STYLE_NO_TITLE; // 用于设置dialogFragment的Style 默认是NO_TITLE
    private float dimAmount;// dimAmount 窗口背景透明度
    private int width; // view的宽
    private int height; // view的高
    private boolean isCancelable; // 按back键是否可取消
    private boolean isCanceledTouchOutSide; // 点击非contentView区域是否可取消
    @LayoutRes
    private int layoutResId;  // 所设置的布局ID
    private int gravity = Gravity.CENTER; // view的gravity
    private String tag; // DialogFragment的tag
    private CloudDialogListeners.OnCallDialogListener mOnCallDialogListener; // 创建Dialog的listener
    private CloudDialogListeners.OnLifecycleListener mOnLifecycleListener = new CloudDialogListeners.OnLifecycleListenerAdapter(); // CloudDialog生命周期监听器
    // dialog相关监听器
    // 重写DialogFragment的相关生命周期
    private DialogInterface.OnDismissListener mOnDismissListener;
    private DialogInterface.OnCancelListener mOnCancelListener;
    // 在DialogFragment.onGetLayoutInflater()中赋值给getDialog().setXXX()
    private DialogInterface.OnKeyListener mOnKeyListener;
    private DialogInterface.OnShowListener mOnShowListener;
    @StyleRes
    private int windowAnimationsResId;
    private boolean isCustomDialog;

    public CloudDialogController() {
        width = WindowManager.LayoutParams.WRAP_CONTENT;
        height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    // 是否是自定义dialog
    public boolean isCustomDialog() {
        return isCustomDialog;
    }

    public int getWindowAnimationsResId() {
        return windowAnimationsResId;
    }

    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    public View getView() {
        return mView;
    }

    public int getDialogThemeId() {
        return dialogThemeId;
    }

    public int getStyle() {
        return style;
    }

    public float getDimAmount() {
        return dimAmount;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    public boolean isCanceledTouchOutSide() {
        return isCanceledTouchOutSide;
    }

    public int getLayoutResId() {
        return layoutResId;
    }

    public int getGravity() {
        return gravity;
    }

    public String getTag() {
        return tag;
    }

    public CloudDialogListeners.OnCallDialogListener getOnCallDialogListener() {
        return mOnCallDialogListener;
    }

    public CloudDialogListeners.OnLifecycleListener getOnLifecycleListener() {
        return mOnLifecycleListener;
    }

    public DialogInterface.OnDismissListener getOnDismissListener() {
        return mOnDismissListener;
    }

    public DialogInterface.OnCancelListener getOnCancelListener() {
        return mOnCancelListener;
    }

    public DialogInterface.OnKeyListener getOnKeyListener() {
        return mOnKeyListener;
    }

    public DialogInterface.OnShowListener getOnShowListener() {
        return mOnShowListener;
    }

    public static class Params {
        private final String DEFAULT_TAG_PREFIX = "CloudDialog_";

        public final FragmentManager mFragmentManager;
        @LayoutRes
        public int layoutResId;
        public View mView;
        @StyleRes
        public int dialogThemeId;
        @IntRange(from = DialogFragment.STYLE_NORMAL,to = DialogFragment.STYLE_NO_INPUT)
        public int style = DialogFragment.STYLE_NO_TITLE;
        public float dimAmount;
        public int width;
        public int height;
        public boolean isCancelable;
        public boolean isCanceledTouchOutSide;
        public int gravity = Gravity.CENTER;
        public String tag;
        @StyleRes
        public int windowAnimationsResId;
        public CloudDialogListeners.OnCallDialogListener mOnCallDialogListener;
        public CloudDialogListeners.OnLifecycleListener mOnLifecycleListener;

        public DialogInterface.OnDismissListener mOnDismissListener;
        public DialogInterface.OnCancelListener mOnCancelListener;

        public DialogInterface.OnKeyListener mOnKeyListener;
        public DialogInterface.OnShowListener mOnShowListener;
        public boolean isCustomDialog = true;

        public Params(FragmentManager mFragmentManager) {
            this.mFragmentManager = mFragmentManager;
        }

        /**
         * 应用设置
         *
         * @param controller 数据保存封装容器类
         */
        public void apply(CloudDialogController controller) {
            controller.mFragmentManager = this.mFragmentManager;
            if (this.layoutResId > 0) {
                controller.layoutResId = this.layoutResId;
            }
            if (this.mView != null) {
                controller.mView = this.mView;
            }
            if (this.dialogThemeId > 0) {
                controller.dialogThemeId = this.dialogThemeId;
            }
            if (this.style >= 0 && this.style <= 3) {
                controller.style = this.style;
            }
            float dimAmount = this.dimAmount;
            if (dimAmount < 0 || dimAmount > 1.0f) {
                dimAmount = 0.3f;
            }
            controller.dimAmount = dimAmount;
            if (this.width != 0) {
                controller.width = this.width;
            }
            if (this.height != 0) {
                controller.height = this.height;
            }

            if (this.windowAnimationsResId > 0) {
                controller.windowAnimationsResId = this.windowAnimationsResId;
            }

            controller.isCustomDialog = this.isCustomDialog;
            controller.isCancelable = this.isCancelable;
            controller.isCanceledTouchOutSide = this.isCanceledTouchOutSide;
            controller.gravity = this.gravity;
            if (tag == null || !CloudDialog.alreadyHasTag(tag)){
                // provide default tag
                tag = DEFAULT_TAG_PREFIX + hashCode();
            }
            controller.tag = this.tag;
            controller.mOnCallDialogListener = this.mOnCallDialogListener;
            if (this.mOnLifecycleListener != null) {
                controller.mOnLifecycleListener = this.mOnLifecycleListener;
            }
            controller.mOnDismissListener = this.mOnDismissListener;
            controller.mOnCancelListener = this.mOnCancelListener;

            controller.mOnKeyListener = this.mOnKeyListener;
            controller.mOnShowListener = this.mOnShowListener;
        }

    }


}

package com.project.baby.clouddialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.HashMap;

/**
 * DialogFragment封装类
 * Created by yosemite on 2018/1/22.
 */

public class CloudDialog extends DialogFragment {
    public static final String TAG = "CloudDialog";

    // 监听器 及 相关参数的保存
    // 如果旋转屏幕，之前的P会变为null --> 参数过来的
    // 最终的目标只要保存P就可以了
    /**
     * 一种思路是调用者实现所有监听接口，这样恢复的时候直接将this设置到listener即可
     * 另一种思路是直接通过map保存对应的key即可
     */
    private static final HashMap<String, CloudDialogController> CONTROLLER_CACHE = new HashMap<>();
    private static final String DIALOG_TAG_KEY = "CLOUD_DIALOG_TAG";

    protected CloudDialogController dialogController;

    private FrameLayout.LayoutParams mLayoutParams;

    protected static boolean alreadyHasTag(String tag) {
        return tag != null && CONTROLLER_CACHE.containsKey(tag);
    }

    public CloudDialog() {
        dialogController = new CloudDialogController();
    }

    /**
     * 修改window的LayoutParams 用于适配大小
     */
    private void wrapWindowLayoutParams() {
        Window window = getDialog().getWindow();
        // 只有背景色和padding都更改后，layout才能真正的math_parent
        // 不设置背景色为透明，但更改了padding，则layout设置为math_parent后四周仍会出现些许margin
        // 只更改背景色不更改padding则layout无任何变化。
        if (dialogController.getOnCallDialogListener() == null || (dialogController.getOnCallDialogListener() != null && dialogController.isCustomDialog())) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getDecorView().setPadding(0, 0, 0, 0);
        }

        boolean viewLayoutParamsWasChanged = false;
        WindowManager.LayoutParams wlp = window.getAttributes();
        int width = dialogController.getWidth();
        int height = dialogController.getHeight();

        if (width != 0 && width >= -2){/*-1 was MATH_PARENT,-2 was WRAP_CONTENT*/
            wlp.width = width;
            if (dialogController.getOnCallDialogListener() == null){
               if (mLayoutParams != null) {
                   mLayoutParams.width = width;
                   viewLayoutParamsWasChanged = true;
               }
            }
        }else {
            // because view's LayoutParams will be null
            // eg: new FrameLayout(getContext()).getLayoutParams() == null was true
            if (mLayoutParams != null) {
                wlp.width = mLayoutParams.width;
            }
        }

        if (height != 0 && height >= -2) {
            if (dialogController.getOnCallDialogListener() == null) {
                if (mLayoutParams != null) {
                    mLayoutParams.height = height;
                    viewLayoutParamsWasChanged = true;
                }
            }
        }else {
            if (mLayoutParams != null) {
                wlp.height = mLayoutParams.height;
            }
        }

        Log.e(TAG,"width :" + wlp.width + ",height: " + wlp.height);

        if (dialogController.getDimAmount() >= 0) {
            wlp.dimAmount = dialogController.getDimAmount();
        }
        wlp.gravity = dialogController.getGravity();
        window.setAttributes(wlp);

        if (viewLayoutParamsWasChanged){
            // need change view's LayoutParams.Let it wrap dialog size.
            getView().setLayoutParams(mLayoutParams);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialogController.getOnDismissListener() != null) {
            dialogController.getOnDismissListener().onDismiss(dialog);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dialogController.getOnDismissListener() != null) {
            dialogController.getOnDismissListener().onDismiss(dialog);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(dialogController.getStyle(), dialogController.getOnCallDialogListener() == null ? dialogController.getDialogThemeId() : 0);

        if (savedInstanceState != null) {
            String controllerKey = savedInstanceState.getString(DIALOG_TAG_KEY);
            dialogController = CONTROLLER_CACHE.remove(controllerKey);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogController.getOnLifecycleListener().onPreInit(savedInstanceState);
        if (dialogController.getOnCallDialogListener() != null) {
            return dialogController.getOnCallDialogListener().getDialog(savedInstanceState);
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View dialogView = dialogController.getView();
        if (dialogView == null) {
            if (dialogController.getLayoutResId() > 0) {
                dialogView = inflater.inflate(dialogController.getLayoutResId(), container, false);
                mLayoutParams = (FrameLayout.LayoutParams) dialogView.getLayoutParams();
            }else {
                if (dialogController.getOnCallDialogListener() == null) {
                    throw new IllegalArgumentException("Please provide view or layout resource id or dialog for CloudDialog!");
                }
            }
        } else {
            // resolve DialogFragment can not be attached to a container view when
            // use Builder.setDialogView method and device configuration was changed.
            if (savedInstanceState != null && dialogView.getParent() != null) {
                ((ViewGroup) dialogView.getParent()).removeView(dialogView);
            }
            mLayoutParams = (FrameLayout.LayoutParams) dialogView.getLayoutParams();
        }
        // onCreateDialogView here
        dialogController.getOnLifecycleListener().onCreateDialogView(getDialog(), dialogView, savedInstanceState);

        return dialogView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.e(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        getDialog().setCancelable(dialogController.isCancelable());
        getDialog().setCanceledOnTouchOutside(dialogController.isCanceledTouchOutSide());

        if (dialogController.getOnKeyListener() != null) {
            getDialog().setOnKeyListener(dialogController.getOnKeyListener());
        }
        if (dialogController.getOnShowListener() != null) {
            getDialog().setOnShowListener(dialogController.getOnShowListener());
        }

        if (dialogController.getWindowAnimationsResId() > 0) {
            getDialog().getWindow().setWindowAnimations(dialogController.getWindowAnimationsResId());
        }

        // post init here
        dialogController.getOnLifecycleListener().onPostInit(getDialog(), dialogController.getView());
    }

    @Override
    public void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
        wrapWindowLayoutParams();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save params
        outState.putString(DIALOG_TAG_KEY, dialogController.getTag());
        dialogController.getOnLifecycleListener().onSaveInstanceState(outState);
        CONTROLLER_CACHE.put(dialogController.getTag(), dialogController);
    }

    public void show() {
        Log.d(TAG, "show Cloud dialog.");
        show(dialogController.getFragmentManager(), dialogController.getTag());
    }

    public static final class Builder {

        private CloudDialogController.Params P;
        private Context mContext;

        public Builder(Context context,FragmentManager fragmentManager) {
            P = new CloudDialogController.Params(fragmentManager);
            mContext = context;
        }

        public Builder(AppCompatActivity activity) {
            this(activity,activity.getSupportFragmentManager());
        }

        public Builder(Fragment fragment) {
            this(fragment.getContext(),fragment.getActivity().getSupportFragmentManager());
        }

        public Builder setLayoutRes(@LayoutRes int layoutResId) {
            P.layoutResId = layoutResId;
            return this;
        }

        public Builder setDialogView(View dialogView) {
            P.mView = dialogView;
            return this;
        }

        public Builder setDialogTheme(@StyleRes int dialogThemeId) {
            P.dialogThemeId = dialogThemeId;
            return this;
        }

        public Builder isCustomDialog(boolean isCustomDialog) {
            P.isCustomDialog = isCustomDialog;
            return this;
        }

        public Builder setDialogStyle(int dialogStyle) {
            P.style = dialogStyle;
            return this;
        }

        public Builder setDimAmount(float dimAmount) {
            P.dimAmount = dimAmount;
            return this;
        }

        public Builder setWidth(int width) {
            if (width > 0) {
                P.width = ScreenUtils.dip2px(mContext,width);
            }else if (width >= -2 && width < 0) {
                P.width = width;
            }
            return this;
        }

        public Builder setHeight(int height) {
            if (height > 0) {
                P.height = ScreenUtils.dip2px(mContext,height);
            }else if (height >= -2 && height < 0) {
                P.height = height;
            }
            return this;
        }

        public Builder setScreenWidthAspect(float widthAspect) {
            if (widthAspect > 0 && widthAspect <= 1.0f) {
                P.width = (int) (ScreenUtils.getScreenWidth(mContext) * widthAspect);
            }
            return this;
        }

        public Builder setScreenHeightAspect(float heightAspect) {
            if (heightAspect > 0 && heightAspect <= 1.0f) {
                P.height = (int) (ScreenUtils.getScreenHeight(mContext) * heightAspect);
            }
            return this;
        }

        public Builder setWindowAnimations(@StyleRes int windowAnimationsResId) {
            P.windowAnimationsResId = windowAnimationsResId;
            return this;
        }

        public Builder setCancelable(boolean isCancelable) {
            P.isCancelable = isCancelable;
            return this;
        }

        public Builder setCanceledTouchOutSide(boolean isCanceledTouchOutSide) {
            P.isCanceledTouchOutSide = isCanceledTouchOutSide;
            return this;
        }

        public Builder setGravity(int gravity) {
            P.gravity = gravity;
            return this;
        }

        public Builder setTag(String tag) {
            P.tag = tag;
            return this;
        }

        public Builder setDialog(CloudDialogListeners.OnCallDialogListener dialogListener) {
            P.mOnCallDialogListener = dialogListener;
            return this;
        }

        public Builder setLifecycleListener(CloudDialogListeners.OnLifecycleListener lifecycleListener) {
            P.mOnLifecycleListener = lifecycleListener;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
            P.mOnDismissListener = dismissListener;
            return this;
        }

        public Builder setOnCancelListener(DialogInterface.OnCancelListener cancelListener) {
            P.mOnCancelListener = cancelListener;
            return this;
        }

        public Builder setOnKeyListener(DialogInterface.OnKeyListener keyListener) {
            P.mOnKeyListener = keyListener;
            return this;
        }

        public Builder setOnShowListener(DialogInterface.OnShowListener showListener) {
            P.mOnShowListener = showListener;
            return this;
        }

        /**
         * 构建dialog
         *
         * @return Cloud Dialog
         */
        public CloudDialog create() {
            Log.i(TAG, "on create Cloud dialog.");
            mContext = null;
            CloudDialog dialog = new CloudDialog();
            P.apply(dialog.dialogController);
            return dialog;
        }

        /**
         * 通过Builder来显示dialog
         *
         * @return Cloud Dialog
         */
        public CloudDialog show() {
            CloudDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

}

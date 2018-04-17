package com.baby.project.projectbaby.base;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

import com.baby.project.projectbaby.base.util.RxLifecycleUtils;
import com.uber.autodispose.AutoDisposeConverter;

public abstract class BasePresenter<V extends IView> implements IPresenter<V> {

    private LifecycleOwner mLifecycleOwner;

    /**
     * 使用auto dispose 监听activity/fragment的生命周期 自动dispose
     * @param <T> Observer 类型
     * @return auto dispose converter
     */
    protected <T> AutoDisposeConverter<T> bindLifecycle() {
        if (null == mLifecycleOwner) {
            throw new NullPointerException("lifecycleOwner was null");
        }

        return RxLifecycleUtils.bindLifecycle(mLifecycleOwner);
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        this.mLifecycleOwner = owner;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        this.mLifecycleOwner = null;
    }

    @Override
    public void onLifecycleChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {
        // can override this method to observer lifecycle
    }
}

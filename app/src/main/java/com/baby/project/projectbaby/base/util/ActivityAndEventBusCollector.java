package com.baby.project.projectbaby.base.util;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity收集器，用于统一存放Activity并注册EventBus，以及作为应用的出口
 */
public class ActivityAndEventBusCollector {

    public static List<LifecycleOwner> sActivities = new ArrayList<>();

    public static void register(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(new CustomLifecycleObserverAdapter() {
            @Override
            void onCreate(@NonNull LifecycleOwner lifecycleOwner) {
                super.onCreate(lifecycleOwner);
                sActivities.add(lifecycleOwner);
            }

            @Override
            void onDestroy(@NonNull LifecycleOwner lifecycleOwner) {
                super.onDestroy(lifecycleOwner);
                sActivities.remove(lifecycleOwner);
            }
        });
    }

    public static void finishAll() {
        for (LifecycleOwner lifecycleOwner : sActivities) {
            if (lifecycleOwner instanceof Activity) {
                Activity activity = (Activity) lifecycleOwner;
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }


}

package com.baby.project.projectbaby.base;

import com.baby.project.projectbaby.base.receiver.ReceiverActionContract;

/**
 * 配置类
 * Created by yosemite on 2018/4/9.
 */

public class ProjectBabyConfig {
    public static final String DATA_TAG_PUSH_DATA = "com.avos.avoscloud.Data";

    private static final boolean isTesterMode = true;
    private static final boolean isDeveloperMode = true;

    public static boolean isTesterMode() {
        return isTesterMode;
    }

    public static boolean isDeveloperMode() {
        return isDeveloperMode;
    }
}

package com.baby.project.projectbaby.base;

/**
 * 配置类
 * Created by yosemite on 2018/4/9.
 */

public class ProjectBabyConfig {

    private static final boolean isTesterMode = true;
    private static final boolean isDeveloperMode = true;

    public static boolean isTesterMode() {
        return isTesterMode;
    }

    public static boolean isDeveloperMode() {
        return isDeveloperMode;
    }
}

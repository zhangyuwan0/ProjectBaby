package com.baby.project.projectbaby.base;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.baby.project.projectbaby.sign.bean.User;

/**
 * LeanCloud初始化工具
 * Created by yosemite on 2018/4/8.
 */

public class InitializeUtil {

    public static void registerSubClasses() {
        // User的子类化
        AVUser.registerSubclass(User.class);
        AVUser.alwaysUseSubUserClass(User.class);
    }




}

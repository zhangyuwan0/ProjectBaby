package com.baby.project.projectbaby;

import com.avos.avoscloud.AVUser;
import com.baby.project.projectbaby.sign.bean.User;

/**
 * Created by yosemite on 2018/4/8.
 */

public class InitializeUtil {

    public static void registerSubClasses() {
        // User的子类化
        AVUser.registerSubclass(User.class);
        AVUser.alwaysUseSubUserClass(User.class);
    }



}

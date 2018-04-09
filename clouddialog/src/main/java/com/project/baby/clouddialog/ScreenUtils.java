package com.project.baby.clouddialog;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * CloudDialog工具类
 * Created by yosemite on 2018/2/26.
 */

public class ScreenUtils {

    public static Point getScreenSize(Context context) {
        Point point = new Point();
        getScreenSize(context, point);
        return point;
    }

    public static void getScreenSize(Context context, Point point) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
    }

    public static int getScreenWidth(Context context) {
        return getScreenSize(context).x;
    }

    public static int getScreenHeight(Context context) {
        return  getScreenSize(context).y;
    }

    public static int dip2px(Context context,float dip){
        return (int) (context.getResources().getDisplayMetrics().density * dip + 0.5f);
    }

    public static int px2dip(Context context,float px){
        return (int) (context.getResources().getDisplayMetrics().density / px + 0.5f);
    }
}

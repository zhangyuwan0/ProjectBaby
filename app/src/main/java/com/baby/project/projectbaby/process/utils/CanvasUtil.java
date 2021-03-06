package com.baby.project.projectbaby.process.utils;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by yosemite on 2018/3/28.
 */

public class CanvasUtil {

    public static final int BASELINE_MODE_TOP = 1;
    public static final int BASELINE_MODE_BOTTOM = 2;
    public static final int BASELINE_MODE_CENTER = 3;

    public static void drawText(Canvas canvas,Paint paint,String content,float x,float y,int baseLineMode) {
        canvas.drawText(content,x,getDrawYForText(paint.getFontMetrics(),y,baseLineMode),paint);
    }

    public static void drawText(Canvas canvas,Paint paint,String content,int start,int end,float x,float y,int baseLineMode) {
        canvas.drawText(content,start,end,x,getDrawYForText(paint.getFontMetrics(),y,baseLineMode),paint);
    }

    public static float getDrawYForText(Paint.FontMetrics metrics, float baseLineY, int baseLineMode) {
        float result;
        float top = metrics.top + baseLineY;
        float bottom = metrics.bottom + baseLineY;
        switch (baseLineMode) {
            case BASELINE_MODE_CENTER:
                float offsetY = (baseLineY - top) * 0.5f;
                result = baseLineY  + offsetY - metrics.bottom;
                break;
            case BASELINE_MODE_BOTTOM:
                //文字高度
                float fontHeight = bottom - top; //注意top为负数
                result = baseLineY + fontHeight / 2.0f;
                break;
            case BASELINE_MODE_TOP:
            default:
                result = baseLineY;
        }
        return result;
    }


}

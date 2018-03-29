package com.baby.project.projectbaby.process.widget;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.baby.project.projectbaby.process.bean.ProcessWrapper;
import com.baby.project.projectbaby.process.bean.Project;

import java.util.List;

/**
 * Created by yosemite on 2018/3/15.
 */

public abstract class AbsProcessChartPainter {


    abstract void drawShutdownContent(Canvas canvas, Paint paint, Project project);

    abstract void drawHeaderBackground(Canvas canvas, Paint paint);

    abstract void drawLeftHeader(Canvas canvas, Paint paint);

    abstract void drawRightHeader(Canvas canvas, Paint paint);

    abstract void drawLeftColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process);

    abstract void drawRightColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process);

    abstract void drawTimeLine(Canvas canvas, Paint paint, Project project);

    abstract void drawProcessContent(Canvas canvas, Paint paint, int position, ProcessWrapper process);

    abstract void drawProcessBackground(Canvas canvas, Paint paint, int position, ProcessWrapper process);

    abstract void drawStartWorkLine(Canvas canvas, Paint paint, Project project);

    abstract void drawCompleteWorkLine(Canvas canvas, Paint paint, Project project);

    abstract void drawTodayLine(Canvas canvas, Paint paint, Project project);

    private void drawHeader(Canvas canvas, Paint paint, Project project) {
        drawHeaderBackground(canvas,paint);
        drawLeftHeader(canvas, paint);
        drawTimeLine(canvas, paint, project);
        drawRightHeader(canvas, paint);
    }

    public abstract void drawProcessList(Canvas canvas, Paint paint, Project project, List<ProcessWrapper> processWrappers);

    private void drawExtraView(Canvas canvas, Paint paint, Project project) {
        if (project != null) {
            drawShutdownContent(canvas, paint, project);
            drawStartWorkLine(canvas, paint, project);
            drawCompleteWorkLine(canvas, paint, project);
        }

        drawTodayLine(canvas, paint, project);
    }

    final void drawChart(Canvas canvas, Paint paint, Project project, List<ProcessWrapper> processWrappers) {
        drawHeader(canvas, paint, project);
        drawProcessList(canvas, paint, project, processWrappers);
        drawExtraView(canvas, paint, project);
    }

}

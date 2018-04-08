package com.baby.project.projectbaby.process.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Scroller;

import com.baby.project.projectbaby.process.bean.Process;
import com.baby.project.projectbaby.process.bean.ProcessWrapper;
import com.baby.project.projectbaby.process.bean.Project;
import com.baby.project.projectbaby.process.utils.CanvasUtil;
import com.baby.project.projectbaby.process.utils.DateUtil;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 横道图
 * Created by yosemite on 2018/3/15.
 */

public class ProcessChartSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "ProgressChart";

    private static final int MIN_REFRESH_MILLIS = 17;

    private SurfaceHolder mSurfaceHolder;
    // 绘制所用画布
    private Canvas mCanvas;
    // 是否正在绘制
    private volatile boolean mIsDrawing;
    // 绘制所用画笔
    private Paint mPaint;

    private Thread mThread;

    private Project project;

    private List<ProcessWrapper> processWrappers;

    private ProcessChartPainter mProcessChartPainter;

    private TouchHelper mTouchHelper;

    private Option mOption;

    public ProcessChartSurfaceView(Context context) {
        super(context);
        initView();
    }

    public ProcessChartSurfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ProcessChartSurfaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this); // 注册surfaceHolder的回调方法

        mOption = Option.getDefaultOption();
        mTouchHelper = new TouchHelper(getContext(), mOption);
        mProcessChartPainter = new ProcessChartPainter(mTouchHelper, mOption);

        initGestureDetector();
        initScaleGestureDetector();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated:width:" + getWidth() + ",height:" + getHeight());

        mIsDrawing = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged:width:" + width + ",height:" + height);
        // 更新配置
        mOption.calculateRealParams(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 停止线程
        try {
            mIsDrawing = false;
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void run() {
        long updateDurationMillis = 0;
        long sleepDurationMillis = 0;
        // 去锯齿
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 开启文本绘制优化
        mPaint.setStrikeThruText(true);
        Project.ShutdownMessage shutdownMessage = new Project.ShutdownMessage();
        shutdownMessage.setBeginTime(new Date(new Date().getTime() + DateUtil.DAY_MILLS * 2));
        shutdownMessage.setEndTime(new Date(new Date().getTime() + DateUtil.DAY_MILLS * 4));
        List<Project.ShutdownMessage> list = new ArrayList<>();
        list.add(shutdownMessage);
        this.project = new Project();
        project.setBeginTime(new Date());
        project.setEndTime(new Date(new Date().getTime() + DateUtil.DAY_MILLS * 100));
        project.setProcesses(new ArrayList<Process>());
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            Process process = new Process();
            process.setProcessBeginTime(new Date());
            process.setProcessEndTime(new Date(new Date().getTime() + DateUtil.DAY_MILLS * 5));
            process.setProcessAlreadyCompletePercent(0.5f);
            process.setProcessCost(1000);
            process.setProcessShutdownTimes(list);
            process.setProcessUseDays(2);
            process.setProcessName(i + "爽宝宝最烦人汪爽宝宝最烦人汪爽宝宝最烦人汪爽宝宝最烦人汪爽宝宝最烦人");
            processes.add(i, process);
        }
        this.project.setProcesses(processes);
        this.processWrappers = ProcessWrapper.convertProcessList(project);
        this.project.setShutdownMessages(list);
        mTouchHelper.refreshData(project);

        while (mIsDrawing) {
            long beforeUpdateRender = System.nanoTime();
            long deltaMillis = sleepDurationMillis + updateDurationMillis;
            updateAndRender(deltaMillis);
            updateDurationMillis = (System.nanoTime() - beforeUpdateRender) / 1000000L;
            // TODO time need test and will be reset
            sleepDurationMillis = Math.max(2, MIN_REFRESH_MILLIS - updateDurationMillis);
            try {
                Thread.sleep(sleepDurationMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateAndRender(long deltaMillis) {
        try {
            mTouchHelper.checkScrollState();
            mCanvas = mSurfaceHolder.lockCanvas();
            // clear canvas
            mCanvas.drawColor(Color.TRANSPARENT);
            // 尚未给数据 不用绘制
            if (null == this.project) {
                return;
            }
            mProcessChartPainter.drawChart(mCanvas, mPaint, this.project, this.processWrappers);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    public synchronized void setContentData(Project project) {
        this.project = project;
        this.processWrappers = ProcessWrapper.convertProcessList(this.project);
    }

    /**
     * chart 配置类
     */
    public static class Option {

        private static final String FIELD_TIME_BAR_DAY_WIDTH = "time_bar_day_width";
        private static final String FIELD_LEFT_COLUMN_WIDTH = "left_column_width";
        private static final String FIELD_RIGHT_COLUMN_WIDTH = "right_column_width";
        private static final String FIELD_CHART_HEADER_HEIGHT = "chart_header_height";
        private static final String FIELD_PROCESS_ITEM_HEIGHT = "process_item_height";
        private static final String FIELD_PROCESS_ESTIMATE_LINE_WIDTH = "process_estimate_line_width";
        private static final String FIELD_PROCESS_NEED_LINE_WIDTH = "process_need_line_width";
        private static final String FIELD_PROCESS_COMPLETE_LINE_WIDTH = "process_complete_line_width";
        private static final String FIELD_TEXT_COLOR = "text_color";
        private static final String FIELD_HEADER_TEXT_COLOR = "header_text_color";
        private static final String FIELD_LINE_COLOR = "line_color";
        private static final String FIELD_BACKGROUND_COLOR = "background_color";
        private static final String FIELD_HEADER_LINE_COLOR = "header_line_color";
        private static final String FIELD_HEADER_BACKGROUND_COLOR = "header_background_color";
        private static final String FIELD_ODD_POSITION_BACKGROUND_COLOR = "odd_position_background_color";
        private static final String FIELD_EVEN_POSITION_BACKGROUND_COLOR = "even_position_background_color";
        private static final String FIELD_TODAY_LINE_COLOR = "today_line_color";
        private static final String FIELD_PROCESS_ESTIMATE_LINE_COLOR = "process_estimate_line_color";
        private static final String FIELD_PROCESS_NEED_LINE_COLOR = "process_need_line_color";
        private static final String FIELD_PROCESS_COMPLETE_LINE_COLOR = "process_complete_line_color";
        private static final String FIELD_LINE_WIDTH = "line_width";
        private static final String FIELD_LEFT_COLUMN_HEADER_BOTTOM_TEXT = "left_column_header_bottom_text";
        private static final String FIELD_LIFT_COLUMN_HEADER_TOP_TEXT = "lift_column_header_top_text";
        private static final String FIELD_RIGHT_COLUMN_HEADER_LEFT_TEXT = "right_column_header_left_text";
        private static final String FIELD_RIGHT_COLUMN_HEADER_RIGHT_TEXT = "right_column_header_right_text";
        private static final String FIELD_PROCESS_COMPLETE_TEXT_COLOR = "process_complete_text_color";
        private static final String FIELD_PROCESS_NEED_TEXT_COLOR = "process_need_text_color";
        private static final String FIELD_HEADER_TEXT_SIZE = "header_text_size";
        private static final String FIELD_TEXT_SIZE = "text_size";
        private static final String FIELD_TIME_BAR_DAY_TEXT_SIZE = "time_bar_day_text_size";
        private static final String FIELD_PROCESS_COMPLETE_TEXT_SIZE = "process_complete_text_size";
        private static final String FIELD_PROCESS_NEED_TEXT_SIZE = "process_need_text_size";
        private static final String FIELD_SHUTDOWN_BACKGROUND_COLOR = "shutdown_background_color";

        /**
         * text
         */
        @SerializedName(FIELD_LEFT_COLUMN_HEADER_BOTTOM_TEXT)
        public String leftColumnHeaderBottomText = "工序";

        @SerializedName(FIELD_LIFT_COLUMN_HEADER_TOP_TEXT)
        public String liftColumnHeaderTopText = "时间";

        @SerializedName(FIELD_RIGHT_COLUMN_HEADER_LEFT_TEXT)
        public String rightColumnHeaderLeftText = "预计";

        @SerializedName(FIELD_RIGHT_COLUMN_HEADER_RIGHT_TEXT)
        public String rightColumnHeaderRightText = "已完成";

        @SerializedName(FIELD_TEXT_SIZE)
        public float textSize = 23f;

        @SerializedName(FIELD_TIME_BAR_DAY_TEXT_SIZE)
        public float timeBarDayTextSize = 15f;


        // TODO 组件宽度/高度获取
        // TODO time bar width 计算 (由上一步结果影响)
        // TODO process list height 计算 (由上一步结果影响)
        public void calculateRealParams(int width, int height) {
            this.width = width;
            this.height = height;
            this.timeBarWidth = this.width - this.leftColumnWidth - this.rightColumnWidth;
            this.processListHeight = this.height - this.chartHeaderHeight;
        }

        public float width;

        public float height;

        /**
         * header
         */
        public float timeBarWidth;

        @SerializedName(FIELD_TIME_BAR_DAY_WIDTH)
        public float timeBarDayWidth = 35f;

        @SerializedName(FIELD_LEFT_COLUMN_WIDTH)
        public float leftColumnWidth = 100;

        @SerializedName(FIELD_RIGHT_COLUMN_WIDTH)
        public float rightColumnWidth = 160;

        @SerializedName(FIELD_CHART_HEADER_HEIGHT)
        public float chartHeaderHeight = 70;
        /**
         * process list
         */
        public float processListHeight;

        @SerializedName(FIELD_PROCESS_ITEM_HEIGHT)
        public float processItemHeight = 100f;

        @SerializedName(FIELD_PROCESS_ESTIMATE_LINE_WIDTH)
        public float processEstimateLineWidth = 10f;

        @SerializedName(FIELD_PROCESS_NEED_LINE_WIDTH)
        public float processNeedLineWidth = 10f;

        @SerializedName(FIELD_PROCESS_COMPLETE_LINE_WIDTH)
        public float processCompleteLineWidth = 10f;

        /**
         * color
         */
        @SerializedName(FIELD_TEXT_COLOR)
        public @ColorInt
        int textColor = Color.BLACK;

        @SerializedName(FIELD_HEADER_TEXT_COLOR)
        public @ColorInt
        int headerTextColor = Color.WHITE;

        @SerializedName(FIELD_LINE_COLOR)
        public @ColorInt
        int lineColor = Color.BLACK;

        @SerializedName(FIELD_BACKGROUND_COLOR)
        public @ColorInt
        int backgroundColor;

        @SerializedName(FIELD_HEADER_LINE_COLOR)
        public @ColorInt
        int headerLineColor = Color.WHITE;

        @SerializedName(FIELD_HEADER_BACKGROUND_COLOR)
        public @ColorInt
        int headerBackgroundColor = 0xFF1F1F26;

        @SerializedName(FIELD_ODD_POSITION_BACKGROUND_COLOR)
        public @ColorInt
        int oddPositionBackgroundColor = Color.DKGRAY;

        @SerializedName(FIELD_EVEN_POSITION_BACKGROUND_COLOR)
        public @ColorInt
        int evenPositionBackgroundColor = Color.GRAY;

        @SerializedName(FIELD_TODAY_LINE_COLOR)
        public @ColorInt
        int todayLineColor = Color.RED;

        @SerializedName(FIELD_PROCESS_ESTIMATE_LINE_COLOR)
        public @ColorInt
        int processEstimateLineColor = Color.BLACK;

        @SerializedName(FIELD_PROCESS_NEED_LINE_COLOR)
        public @ColorInt
        int processNeedLineColor = Color.RED;

        @SerializedName(FIELD_PROCESS_COMPLETE_LINE_COLOR)
        public @ColorInt
        int processCompleteLineColor = Color.GREEN;

        @SerializedName(FIELD_PROCESS_COMPLETE_TEXT_COLOR)
        public @ColorInt
        int processCompleteTextColor = Color.GREEN;


        @SerializedName(FIELD_PROCESS_NEED_TEXT_COLOR)
        public @ColorInt
        int processNeedTextColor = Color.RED;

        /**
         * extra
         */
        @SerializedName(FIELD_LINE_WIDTH)
        public float lineWidth = 1.2f;

        @SerializedName(FIELD_HEADER_TEXT_SIZE)
        public float headerTextSize = 23.0f;

        @SerializedName(FIELD_PROCESS_COMPLETE_TEXT_SIZE)
        public float processCompleteTextSize = 23.0f;

        @SerializedName(FIELD_PROCESS_NEED_TEXT_SIZE)
        public float processNeedTextSize = 23.0f;

        @SerializedName(FIELD_SHUTDOWN_BACKGROUND_COLOR)
        public @ColorInt
        int shutdownBackgroundColor = Color.YELLOW;

        public static Option getDefaultOption() {
            Option defaultOption = new Option();
            // TODO write default value
            return defaultOption;
        }

    }

    private static class ProcessChartPainter extends AbsProcessChartPainter {

        private static final float[] DASH_PARAMS = new float[]{6, 6};

        private TouchHelper mTouchHelper;

        private Option mOption;

        private DashPathEffect mDashPathEffect;

        private Date today;

        public ProcessChartPainter(TouchHelper touchHelper, Option option) {
            mTouchHelper = touchHelper;
            mOption = option;
            mDashPathEffect = new DashPathEffect(DASH_PARAMS, 0);
            today = DateUtil.getDayFromDate(new Date());
        }

        public void setOption(Option option) {
            mOption = option;
        }

        @Override
        void drawShutdownContent(Canvas canvas, Paint paint, Project project) {
            List<Project.ShutdownMessage> shutdownMessages = project.getShutdownMessages();
            if (null == shutdownMessages) {
                return;
            }

            long projectBeginTime = project.getBeginTime().getTime();
            long projectEndDay = DateUtil.getDateDiffWithDay(projectBeginTime, project.getEndTime().getTime());
            long curDayInProject = DateUtil.getDateDiffWithDay(projectBeginTime, mTouchHelper.getCurrentTime());
            long shownEndDayInProject = curDayInProject + offsetDay;
            int size = shutdownMessages.size();
            float processListRealHeight = mOption.processItemHeight * project.getProcesses().size() * mTouchHelper.scaleFactor;
            for (int i = 0; i < size; i++) {
                Project.ShutdownMessage shutdownMessage = shutdownMessages.get(i);
                long shutDownBeginDayInProject = Math.max(DateUtil.getDateDiffWithDay(projectBeginTime, shutdownMessage.getBeginTime().getTime()), 0);
                long shutDownEndDayInProject = Math.min(DateUtil.getDateDiffWithDay(projectBeginTime, shutdownMessage.getEndTime().getTime()), projectEndDay);
                // 在显示范围内，才绘制
                if (shutDownEndDayInProject >= curDayInProject && shutDownBeginDayInProject <= shownEndDayInProject) {
                    canvas.save();
                    canvas.clipRect(mOption.leftColumnWidth, mOption.chartHeaderHeight, mOption.width - mOption.rightColumnWidth, mOption.chartHeaderHeight + processListRealHeight);
                    canvas.translate(mOption.leftColumnWidth - mTouchHelper.getScrollX() * mTouchHelper.scaleFactor + (shutDownBeginDayInProject - 1) * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, mOption.chartHeaderHeight);
                    setBackgroundPaintStyle(paint, mOption.shutdownBackgroundColor);
                    canvas.drawRect(0, 0, (shutDownEndDayInProject - shutDownBeginDayInProject + 1) * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, Math.min(mOption.processListHeight, processListRealHeight), paint);
                    canvas.restore();
                } else {
                    break;
                }
            }

        }

        @Override
        void drawHeaderBackground(Canvas canvas, Paint paint) {
            setBackgroundPaintStyle(paint, mOption.headerBackgroundColor);
            canvas.drawRect(0f, 0f, mOption.width, mOption.chartHeaderHeight, paint);
        }

        @Override
        void drawLeftHeader(Canvas canvas, Paint paint) {
            // border
            setLinePaintStyle(paint, mOption.headerLineColor, mOption.lineWidth);
            drawBorder(canvas, paint, 0f, 0f, mOption.leftColumnWidth, mOption.chartHeaderHeight);
            // split line
            canvas.drawLine(0f, 0f, mOption.leftColumnWidth, mOption.chartHeaderHeight, paint);
            // bottom text
            float middleHeight = mOption.chartHeaderHeight * 0.5f;
            float middleWidth = mOption.leftColumnWidth * 0.5f;
            float bottomTextX = middleWidth * 0.5f;
            float topTextX = mOption.leftColumnWidth - middleWidth * 0.5f;
            setTextPaintStyle(paint, Paint.Align.CENTER, mOption.headerTextColor, mOption.headerTextSize);
            CanvasUtil.drawText(canvas, paint, mOption.leftColumnHeaderBottomText, bottomTextX, middleHeight, CanvasUtil.BASELINE_MODE_BOTTOM);
            CanvasUtil.drawText(canvas, paint, mOption.liftColumnHeaderTopText, topTextX, middleHeight, CanvasUtil.BASELINE_MODE_TOP);
        }

        private void setTextPaintStyle(Paint paint, Paint.Align align, @ColorInt int textColor, float textSize) {
            paint.setTextAlign(align);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(textColor);
            paint.setTextSize(textSize);
        }

        private void setLinePaintStyle(Paint paint, @ColorInt int lineColor, float lineWidth) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(lineColor);
            paint.setStrokeWidth(lineWidth);
        }

        private void setBackgroundPaintStyle(Paint paint, @ColorInt int bgColor) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(bgColor);
        }

        private void drawBorder(Canvas canvas, Paint paint, float left, float top, float right, float bottom) {
            drawHorizontalLine(canvas, paint, left, right, top);
            drawVerticalLine(canvas, paint, top, bottom, left);
            drawHorizontalLine(canvas, paint, left, right, bottom);
            drawVerticalLine(canvas, paint, top, bottom, right);
        }

        private void drawHorizontalLine(Canvas canvas, Paint paint, float startX, float endX, float lineY) {
            canvas.drawLine(startX, lineY, endX, lineY, paint);
        }

        private void drawVerticalLine(Canvas canvas, Paint paint, float startY, float endY, float lineX) {
            canvas.drawLine(lineX, startY, lineX, endY, paint);
        }


        @Override
        void drawRightHeader(Canvas canvas, Paint paint) {
            // border
            setLinePaintStyle(paint, mOption.headerLineColor, mOption.headerLineColor);
            drawBorder(canvas, paint, mOption.width - mOption.rightColumnWidth, 0, mOption.width, mOption.chartHeaderHeight);
            // 中间线绘制
            float halfWidth = mOption.rightColumnWidth * 0.5f;
            float middleX = mOption.width - halfWidth;
            float middleY = mOption.chartHeaderHeight * 0.5f;
            float offsetX = halfWidth * 0.5f;
            drawVerticalLine(canvas, paint, 0, mOption.chartHeaderHeight, middleX);
            // 文字 | 文字
            setTextPaintStyle(paint, Paint.Align.CENTER, mOption.headerTextColor, mOption.headerTextSize);
            CanvasUtil.drawText(canvas, paint, mOption.rightColumnHeaderLeftText, middleX - offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
            CanvasUtil.drawText(canvas, paint, mOption.rightColumnHeaderRightText, middleX + offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
        }

        private static final float PADDING = 10f;
        private static final String OMIT = "...";

        @Override
        void drawLeftColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process, float startY, float endY) {
            /** 显示不下的文字使用...代替
             *  1.知道一个格子能显示几行文字
             *  2.知道一个格子的一行能显示几个字
             *  3.知道最多显示几个字
             */
            // FIXME 为表格长度相关属性提供 最大值 和 最小值
            String processName = process.getProcess().getProcessName();
            setTextPaintStyle(paint, Paint.Align.LEFT, mOption.textColor, mOption.textSize);
            float rowHeight = paint.descent() - paint.ascent();
            float realHeight = mOption.processItemHeight * mTouchHelper.scaleFactor - PADDING * 2f;
            float realWidth = mOption.leftColumnWidth - PADDING * 2f;
            float centerY = startY + mOption.processItemHeight * 0.5f * mTouchHelper.scaleFactor;
            int rowSumCount = (int) (realHeight / rowHeight);
            float textStartY = centerY - (rowHeight * rowSumCount * 0.5f);
            int index = 0;

            setLinePaintStyle(paint, mOption.lineColor, mOption.lineWidth);
            drawBorder(canvas, paint, 0, startY, mOption.leftColumnWidth, startY + mOption.processItemHeight * mTouchHelper.scaleFactor);

            for (int i = 0; i < rowSumCount; i++) {
                int endIndex = paint.breakText(processName, false, realWidth, null);
                if (i == rowSumCount - 1) {
                    if (index + endIndex < processName.length()) {
                        if (endIndex >= 1) {
                            processName = processName.substring(0, index + endIndex - 1).concat(OMIT);
                            endIndex += 2;
                        } else {
                            processName = processName.concat(OMIT);
                            endIndex += 3;
                        }
                    }
                }
                setTextPaintStyle(paint, Paint.Align.LEFT, mOption.textColor, mOption.textSize);
                CanvasUtil.drawText(canvas, paint, processName, index, index + endIndex, PADDING, textStartY, CanvasUtil.BASELINE_MODE_BOTTOM);
                textStartY += rowHeight;
                index += endIndex;
            }
        }

        private static final String PERCENT_END_FIX = "%";

        @Override
        void drawRightColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process, float startY, float endY) {
            float middleX = mOption.width - mOption.rightColumnWidth * 0.5f;
            float middleY = startY + mOption.processItemHeight * 0.5f * mTouchHelper.scaleFactor;
            float offsetX = mOption.rightColumnWidth * 0.25f;
            // border
            setLinePaintStyle(paint, mOption.lineColor, mOption.lineWidth);
            drawBorder(canvas, paint, mOption.width - mOption.rightColumnWidth, startY, mOption.width, endY);
            // split line
            drawVerticalLine(canvas, paint, startY, endY, middleX);
            // text
            setTextPaintStyle(paint, Paint.Align.CENTER, mOption.processNeedTextColor, mOption.processNeedTextSize);
            CanvasUtil.drawText(canvas, paint, +process.getNeedCompletePercent() * 100f + PERCENT_END_FIX, middleX - offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
            paint.setColor(mOption.processCompleteTextColor);
            paint.setTextSize(mOption.processCompleteTextSize);
            CanvasUtil.drawText(canvas, paint, process.getProcess().getProcessAlreadyCompletePercent() * 100f + PERCENT_END_FIX, middleX + offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
        }

        private static final String DAY_END_FIX = "号";
        private static final String YEAR_CHINESE = "年";
        private static final String MONTH_CHINESE = "月";
        private int offsetDay = 0;                         // 绘制了几个天

        @Override
        void drawTimeLine(Canvas canvas, Paint paint, Project project) {
            /**
             * 1.绘制月份
             * 2.绘制天
             */
            float timeBarShowStartX = mOption.leftColumnWidth;
            float timeBarShowEndX = mOption.width - mOption.rightColumnWidth;
            float timeBarShowStartY = 0f;
            float timeBarShowEndY = mOption.chartHeaderHeight;
            Calendar calendar = mTouchHelper.mCalendar;
            calendar.setTimeInMillis(mTouchHelper.getCurrentTime());
            float timeBarItemStartX = timeBarShowStartX - mTouchHelper.getOffsetX();
            float timeBarMonthStartX = timeBarItemStartX;
            float timeBarItemHeight = mOption.chartHeaderHeight * 0.5f;
            float itemMiddleX = mOption.timeBarDayWidth * mTouchHelper.scaleFactor * 0.5f;
            float dayStartY = mOption.chartHeaderHeight * 0.75f;
            float monthStartY = mOption.chartHeaderHeight * 0.25f;
            canvas.save();
            // clip
            canvas.clipRect(timeBarShowStartX, timeBarShowStartY, timeBarShowEndX, timeBarShowEndY);
            // month/day split line
            setLinePaintStyle(paint, mOption.headerLineColor, mOption.lineWidth);
            drawHorizontalLine(canvas, paint, timeBarShowStartX, timeBarShowEndX, mOption.chartHeaderHeight * 0.5f);
            // firstMont
            int curDay = calendar.get(Calendar.DAY_OF_MONTH);
            if (curDay > 1) {
                // month content
                drawMonthContent(canvas, paint, calendar, timeBarMonthStartX, monthStartY);
            }
            offsetDay = 0;
            while (timeBarItemStartX < timeBarShowEndX) {
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                if (day == 1) {
                    timeBarMonthStartX = timeBarItemStartX;
                    // month content
                    drawMonthContent(canvas, paint, calendar, timeBarMonthStartX, monthStartY);
                    // split line
                    setLinePaintStyle(paint, mOption.headerLineColor, mOption.lineWidth);
                    drawVerticalLine(canvas, paint, 0, mOption.chartHeaderHeight, timeBarItemStartX);
                } else {
                    // split line
                    setLinePaintStyle(paint, mOption.headerLineColor, mOption.lineWidth);
                    drawVerticalLine(canvas, paint, timeBarItemHeight, mOption.chartHeaderHeight, timeBarItemStartX);
                }
                // day content
                setTextPaintStyle(paint, Paint.Align.CENTER, mOption.headerTextColor, mOption.timeBarDayTextSize * mTouchHelper.scaleFactor);
                CanvasUtil.drawText(canvas, paint, day + DAY_END_FIX, timeBarItemStartX + itemMiddleX, dayStartY, CanvasUtil.BASELINE_MODE_CENTER);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                timeBarItemStartX += mOption.timeBarDayWidth * mTouchHelper.scaleFactor;
                offsetDay++;
            }
            canvas.restore();
            // border
            setLinePaintStyle(paint, mOption.headerLineColor, mOption.lineWidth);
            drawBorder(canvas, paint, timeBarShowStartX, timeBarShowStartY, timeBarShowEndX, timeBarShowEndY);
        }

        private void drawMonthContent(Canvas canvas, Paint paint, Calendar calendar, float timeBarMonthStartX, float monthStartY) {
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int overDay = calendar.getMaximum(Calendar.DAY_OF_MONTH);
            float monthEndX = Math.min(timeBarMonthStartX + (overDay - currentDay) * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, mOption.width - mOption.rightColumnWidth);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String monthText = year + YEAR_CHINESE + (month + 1) + MONTH_CHINESE;
            float textWidth = paint.measureText(monthText);
            if (monthEndX < timeBarMonthStartX + textWidth) {
                Paint.Align align;
                if (monthEndX == mOption.width - mOption.rightColumnWidth) {
                    // last month
                    align = Paint.Align.LEFT;
                    setTextPaintStyle(paint, align, mOption.headerTextColor, mOption.timeBarDayTextSize * mTouchHelper.scaleFactor);
                    CanvasUtil.drawText(canvas, paint, monthText, timeBarMonthStartX, monthStartY, CanvasUtil.BASELINE_MODE_CENTER);
                } else {
                    // first month
                    align = Paint.Align.RIGHT;
                    setTextPaintStyle(paint, align, mOption.headerTextColor, mOption.timeBarDayTextSize * mTouchHelper.scaleFactor);
                    CanvasUtil.drawText(canvas, paint, monthText, monthEndX, monthStartY, CanvasUtil.BASELINE_MODE_CENTER);
                }


            } else {

                setTextPaintStyle(paint, Paint.Align.CENTER, mOption.headerTextColor, mOption.timeBarDayTextSize * mTouchHelper.scaleFactor);
                CanvasUtil.drawText(canvas, paint, monthText, timeBarMonthStartX + (monthEndX - timeBarMonthStartX) * 0.5f, monthStartY, CanvasUtil.BASELINE_MODE_CENTER);
            }
        }

        @Override
        void drawProcessContent(Canvas canvas, Paint paint, int position, ProcessWrapper process, float startY, float endY) {
            canvas.save();
            float startX = mOption.leftColumnWidth - mTouchHelper.getScrollX() * mTouchHelper.scaleFactor;
            float endX = mOption.width - mOption.rightColumnWidth;
            float offsetY = mOption.processItemHeight * 0.25f * mTouchHelper.scaleFactor;
            float lineStartX = process.getBeginDay() * mOption.timeBarDayWidth * mTouchHelper.scaleFactor;
            canvas.translate(startX, startY + offsetY);
            setLinePaintStyle(paint, mOption.processEstimateLineColor, mOption.processEstimateLineWidth * mTouchHelper.scaleFactor);
            drawHorizontalLine(canvas, paint, lineStartX, process.getEndDay() * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, 0);
            canvas.translate(0, offsetY * 2f);
            setLinePaintStyle(paint, mOption.processNeedLineColor, mOption.processNeedLineWidth * mTouchHelper.scaleFactor);
            drawHorizontalLine(canvas, paint, lineStartX, process.getNeedCompletePercentDays() * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, 0);
            setLinePaintStyle(paint, mOption.processCompleteLineColor, mOption.processCompleteLineWidth * mTouchHelper.scaleFactor);
            drawHorizontalLine(canvas, paint, lineStartX, process.getAlreadyCompletePercentDays() * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, 0);
            canvas.restore();
            // 最后绘制边框，避免以上线覆盖
            setLinePaintStyle(paint, mOption.lineColor, mOption.lineWidth);
            drawBorder(canvas, paint, mOption.leftColumnWidth, startY, endX, endY);
        }

        @Override
        void drawProcessBackground(Canvas canvas, Paint paint, int position, ProcessWrapper process, float startY, float endY) {
            setBackgroundPaintStyle(paint, position % 2 == 0 ? mOption.evenPositionBackgroundColor : mOption.oddPositionBackgroundColor);
            canvas.drawRect(0, startY, mOption.width, endY, paint);
        }

        private void setDashLinePaintStyle(Paint paint, @ColorInt int lineColor, float lineWidth) {
            paint.setPathEffect(mDashPathEffect);
            setLinePaintStyle(paint, lineColor, lineWidth);
        }

        @Override
        void drawTodayLine(Canvas canvas, Paint paint, Project project) {
            // FIXME today的更新滞后问题
            // project was complete , so can not draw today line
            if (DateUtil.getDateDiffNotAbs(today, project.getEndTime()) > 0) {
                return;
            }
            // update date
            if (DateUtil.getDateDiff(project.getToday(), today) != 0) {
                today = DateUtil.getDayFromDate(new Date(project.getToday().getTime()));
            }

            float processListRealHeight = mOption.processItemHeight * project.getProcesses().size() * mTouchHelper.scaleFactor;
            canvas.save();
            canvas.clipRect(mOption.leftColumnWidth, mOption.chartHeaderHeight, mOption.width - mOption.rightColumnWidth, mOption.chartHeaderHeight + processListRealHeight);
            canvas.translate(mOption.leftColumnWidth + -mTouchHelper.getScrollX() * mTouchHelper.scaleFactor + (DateUtil.getDateDiff(project.getBeginTime(), today) + 1f) * mOption.timeBarDayWidth * mTouchHelper.scaleFactor, mOption.chartHeaderHeight);
            setDashLinePaintStyle(paint, mOption.todayLineColor, mOption.lineWidth);
            canvas.drawLine(0, 0, 0, Math.min(mOption.processListHeight, processListRealHeight), paint);
            paint.reset();
            canvas.restore();
        }

        @Override
        public void drawProcessList(Canvas canvas, Paint paint, Project project, List<ProcessWrapper> processWrappers) {
            if (processWrappers == null) {
                return;
            }

            int curPosition = mTouchHelper.getCurrentPosition();
            // 1.先根据position绘制背景
            float curY = 0f - mTouchHelper.getOffsetY();
            int index = curPosition;
            int size = processWrappers.size();
            float scaleFactor = mTouchHelper.getScaleFactor();
            while (curY < mOption.processListHeight && index < size) {
                ProcessWrapper processWrapper = processWrappers.get(index);
                float startY = mOption.chartHeaderHeight + curY;
                float clipStartY = Math.max(startY, mOption.chartHeaderHeight);
                float endY = startY + mOption.processItemHeight * scaleFactor;
                float clipEndY = Math.min(endY, mOption.height);

                canvas.save();
                // 裁剪边缘避免画出界
                canvas.clipRect(0, clipStartY, mOption.width, clipEndY);
                drawProcessBackground(canvas, paint, index, processWrapper, startY, endY);
                canvas.restore();
                canvas.save();
                canvas.clipRect(0, clipStartY, mOption.leftColumnWidth, clipEndY);
                drawLeftColumn(canvas, paint, index, processWrapper, startY, endY);
                canvas.restore();
                canvas.save();
                canvas.clipRect(mOption.leftColumnWidth, clipStartY, mOption.width - mOption.rightColumnWidth, clipEndY);
                drawProcessContent(canvas, paint, index, processWrapper, startY, endY);
                canvas.restore();
                canvas.save();
                canvas.clipRect(mOption.width - mOption.rightColumnWidth, clipStartY, mOption.width, clipEndY);
                drawRightColumn(canvas, paint, index, processWrapper, startY, endY);
                canvas.restore();
                index++;
                curY += mOption.processItemHeight * scaleFactor;
            }
        }

    }

    public interface OnClickListener {
        void onClick(int position, Project project, ProcessWrapper wrapper);
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    private GestureDetector mGestureDetector;

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                mTouchHelper.abortScroll();
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                /**
                 * 判断点击的是哪一个item，点击后触发listener
                 */
                int position = mTouchHelper.getIndexWithCoordinate(e.getX(), e.getY());
                if (position != -1 && position < processWrappers.size()) {
                    Log.e(TAG, "onClick" + position);
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(position, project, processWrappers.get(position));
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                mTouchHelper.scrollBy(distanceX * 0.5f, distanceY);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                onSingleTapUp(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                mTouchHelper.onFling((int) (-velocityX * 0.5f), (int) -velocityY);
                return true;
            }
        });
    }

    private ScaleGestureDetector mScaleGestureDetector;

    private void initScaleGestureDetector() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mTouchHelper.setScaleFactor(detector.getScaleFactor());
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }


        });
    }

    private static class TouchHelper {

        private static final float MIN_SCALE_FACTOR = 0.5f;
        private static final float MAX_SCALE_FACTOR = 2.0f;

        private Calendar mCalendar;

        /**
         * X轴滑动值
         * 取值范围: [不滑动, 最后一个TimeBarItem全部可见]
         * 当手指向右时，scrollX变大
         * 当手指向左时，scrollX变小
         */
        private float scrollX;

        /**
         * Y轴滑动值
         * 取值范围: [不滑动, 最后一个ProcessItem全部可见 ]
         * 当手指向上时，scrollY变小
         * 当手指向下时，scrollY变大
         */
        private float scrollY;

        /**
         * X轴绘制偏移值
         * offsetX = scrollX - ((long) scrollX)
         * 即取scrollX小数部分
         */
        private float offsetX;

        /**
         * Y轴绘制偏移值
         * offsetY = scrollY - ((long) scrollY)
         * 即取scrollY小数部分
         */
        private float offsetY;

        private long sumDayInProject;

        private int processListSize;

        private float scaleFactor = 1.0f;

        private Date projectBeginTime;

        private Date projectEndTime;

        private Option mOption;

        private Scroller mScroller;


        public TouchHelper(Context context, Option option) {
            mCalendar = Calendar.getInstance();
            mScroller = new Scroller(context);
            mOption = option;
        }

        int getIndexWithCoordinate(float x, float y) {
            if (isInProcessListRange(x, y)) {
                return (int) ((y - mOption.chartHeaderHeight + this.scrollY * scaleFactor) / (mOption.processItemHeight * scaleFactor));
            } else {
                return -1;
            }
        }


        void checkScrollState() {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
        }

        void onFling(int vx, int vy) {
            mScroller.fling((int) this.scrollX, (int) this.scrollY, vx, vy, (int) getMinScrollX(), (int) getMaxScrollX(), (int) getMinScrollY(), (int) getMaxScrollY());
        }

        void abortScroll() {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }

        /**
         * 判断坐标是否在工序列表点击范围内
         *
         * @param x x轴坐标
         * @param y y轴坐标
         * @return is in range of process list
         */
        private boolean isInProcessListRange(float x, float y) {
            return (x >= 0 && x <= mOption.width) && (y >= mOption.chartHeaderHeight && y <= mOption.height);
        }


        public void setOption(Option option) {
            mOption = option;
        }

        void timeBarScrollToTimeWithYear(int year, int month) {
            timeBarScrollToTimeWithDate(new Date(DateUtil.getDateWithYearAndMonth(projectBeginTime, projectEndTime, year, month)));

        }

        void timeBarScrollToTimeWithMonth(int year) {
            timeBarScrollToTimeWithDate(new Date(DateUtil.getDateWithYear(projectBeginTime, projectEndTime, year)));
        }

        void timeBarScrollToTimeWithDate(Date date) {


        }

        public void scrollBy(float distanceX, float distanceY) {
            scrollTo(this.scrollX + distanceX, this.scrollY + distanceY);
        }

        public void scrollTo(float scrollX, float scrollY) {
            setScrollX(scrollX);
            setScrollY(scrollY);
        }


        public void refreshData(Project project) {
            sumDayInProject = DateUtil.getDateDiff(project.getEndTime(), project.getBeginTime());
            processListSize = project.getProcesses().size();
            scaleFactor = 1.0f;
            projectBeginTime = DateUtil.getDayFromDate(project.getBeginTime());
            projectEndTime = DateUtil.getDayFromDate(project.getEndTime());
        }

        /**
         * 根据scrollX 获取时间轴起始点对应的 所在工程中天数
         *
         * @param scrollX x轴滚动值
         * @return 所在工程天数
         */
        public int getDayInProjectWithScrollX(float scrollX) {
            int dayInProject = (int) (scrollX / mOption.timeBarDayWidth);
            return (int) getValueInRange(dayInProject, 0, sumDayInProject);
        }


        /**
         * 根据scrollY 获取工序列表起始点对应的 所在processList中的index
         *
         * @param scrollY y轴滚动值
         * @return index in process list
         */
        public int getPositionInProcessListWithScrollY(float scrollY) {
            int position = (int) (scrollY / mOption.processItemHeight);
            return (int) getValueInRange(position, 0, processListSize);
        }

        /**
         * 获取当前时间轴时间
         *
         * @return 当前时间轴起点所显示时间
         */
        public long getCurrentTime() {
            return DateUtil.convertProjectDayToDate(projectBeginTime, projectEndTime, getDayInProjectWithScrollX(scrollX));
        }

        /**
         * 获取当前index in process list
         *
         * @return 所在process list中index
         */
        public int getCurrentPosition() {
            return getPositionInProcessListWithScrollY(scrollY);
        }

        /**
         * 获取可用值(在[minValue,maxValue]范围内的)
         *
         * @param sourceValue target
         * @param minValue    最小值
         * @param maxValue    最大值
         * @return 可用滚动值 minValue <= result <= maxValue
         */
        private float getValueInRange(float sourceValue, float minValue, float maxValue) {
            return Math.max(minValue, Math.min(sourceValue, maxValue));
        }

        /**
         * 根据所在工程内天数和在时间轴上的x的值来获取scrollX
         *
         * @param dayInProject 所在工程内天数
         * @param xInTimeBar   在时间轴上的x的值(以时间轴绘制起点X轴坐标为起点0)
         * @return scrollX minScrollX <= result <= maxScrollX
         */
        public float getScrollX(int dayInProject, float xInTimeBar) {
            return getValueInRange(dayInProject * mOption.timeBarDayWidth - xInTimeBar, getMinScrollX(), getMaxScrollX());
        }

        /**
         * 根据position和在工序列表上的y的值来获取scrollY
         *
         * @param positionInProcessList position
         * @param yInProcessListView    在工序列表上的y的值(以工序列表绘制起点Y轴坐标为起点0)
         * @return scrollY minScrollY <= result <= maxScrollY
         */
        public float getScrollY(int positionInProcessList, float yInProcessListView) {
            return getValueInRange(positionInProcessList * mOption.processListHeight - yInProcessListView, getMinScrollY(), getMaxScrollY());
        }


        public float getScrollX() {
            return scrollX;
        }

        public void setScrollX(float scrollX) {
            this.scrollX = getValueInRange(scrollX, getMinScrollX(), getMaxScrollX());
            this.offsetX = (this.scrollX % mOption.timeBarDayWidth) * scaleFactor;
        }

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = getValueInRange(this.scaleFactor * scaleFactor, MIN_SCALE_FACTOR, MAX_SCALE_FACTOR);
            setScrollX(this.scrollX);
            setScrollY(this.scrollY);
        }

        public float getScaleFactor() {
            return this.scaleFactor;
        }

        public float getScrollY() {
            return scrollY;
        }

        public void setScrollY(float scrollY) {
            this.scrollY = getValueInRange(scrollY, getMinScrollY(), getMaxScrollY());
            this.offsetY = (this.scrollY % mOption.processItemHeight) * scaleFactor;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public float getMaxScrollX() {
            // + 1是为了能看到结尾 为什么要 / scaleFactor 因为scroll的消费能力与缩放参数成反比？
            return Math.max((sumDayInProject + 1) * mOption.timeBarDayWidth * scaleFactor - mOption.timeBarWidth, 0) / scaleFactor;
        }

        // TODO 可能计算错误
        public float getMaxScrollY() {
            return Math.max(processListSize * mOption.processItemHeight * scaleFactor - mOption.processListHeight, 0) / scaleFactor;
        }

        public float getMinScrollX() {
            return 0;
        }

        public float getMinScrollY() {
            return 0;
        }
    }

}

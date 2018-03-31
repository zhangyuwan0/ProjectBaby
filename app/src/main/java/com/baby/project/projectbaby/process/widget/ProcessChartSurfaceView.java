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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Scroller;

import com.baby.project.projectbaby.process.bean.ProcessWrapper;
import com.baby.project.projectbaby.process.bean.Project;
import com.baby.project.projectbaby.process.utils.CanvasUtil;
import com.baby.project.projectbaby.process.utils.DateUtil;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 横道图
 * Created by yosemite on 2018/3/15.
 */

public class ProcessChartSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final String TAG = "ProgressChart";

    private static final int MIN_REFRESH_MILLIS = 50;

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
        mOption = Option.getDefaultOption();
        mTouchHelper = new TouchHelper(getContext(), mOption);
        mProcessChartPainter = new ProcessChartPainter(mTouchHelper, mOption);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this); // 注册surfaceHolder的回调方法
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated:width-" + getWidth() + ",height-" + getHeight());

        mIsDrawing = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged:width-" + width + ",height-" + height);
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

        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        long updateDurationMillis = 0;
        long sleepDurationMillis = 0;
        // 去锯齿
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
            mCanvas = mSurfaceHolder.lockCanvas();
            // clear canvas
            mCanvas.drawColor(Color.WHITE);
            mProcessChartPainter.drawHeaderBackground(mCanvas, mPaint);
            mProcessChartPainter.drawLeftHeader(mCanvas, mPaint);
            mProcessChartPainter.drawRightHeader(mCanvas, mPaint);
            ProcessWrapper processWrapper = new ProcessWrapper();
            for (int i = 0; i < 8; i++) {
                mProcessChartPainter.drawRightColumn(mCanvas,mPaint,i,processWrapper);
            }
            // 尚未给数据 不用绘制
            if (null == this.project) {
//                return;
            }
//            mProcessChartPainter.drawChart(mCanvas, mPaint, this.project, this.processWrappers);
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
        private static final String FIELD_PROCESS_ESTIMATE_LINE_HEIGHT = "process_estimate_line_height";
        private static final String FIELD_PROCESS_NEED_LINE_HEIGHT = "process_need_line_height";
        private static final String FIELD_PROCESS_COMPLETE_LINE_HEIGHT = "process_complete_line_height";
        private static final String FIELD_TEXT_COLOR = "text_color";
        private static final String FIELD_HEADER_TEXT_COLOR = "header_text_color";
        private static final String FIELD_LINE_COLOR = "line_color";
        private static final String FIELD_BACKGROUND_COLOR = "background_color";
        private static final String FIELD_HEADER_LINE_COLOR = "header_line_color";
        private static final String FIELD_HEADER_BACKGROUND_COLOR = "header_background_color";
        private static final String FIELD_ODD_POSITION_BACKGROUND_COLOR = "odd_position_background_color";
        private static final String FIELD_EVEN_POSITION_BACKGROUND_COLOR = "even_position_background_color";
        private static final String FIELD_START_WORK_LINE_COLOR = "start_work_line_color";
        private static final String FIELD_COMPLETE_WORK_LINE_COLOR = "complete_work_line_color";
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
        private static final String FIELD_PROCESS_COMPLETE_TEXT_SIZE = "process_complete_text_size";
        private static final String FIELD_PROCESS_NEED_TEXT_SIZE = "process_need_text_size";

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

        // TODO 组件宽度/高度获取
        // TODO time bar width 计算 (由上一步结果影响)
        // TODO process list height 计算 (由上一步结果影响)
        public void calculateRealParams(int width, int height) {
            this.width = width;
            this.height = height;
            this.timeBarWidth = width - this.leftColumnWidth - this.rightColumnWidth;
            this.processListHeight = height - this.chartHeaderHeight;
        }

        public float width;

        public float height;

        /**
         * header
         */
        public float timeBarWidth;

        @SerializedName(FIELD_TIME_BAR_DAY_WIDTH)
        public float timeBarDayWidth;

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
        public float processItemHeight = 70;

        @SerializedName(FIELD_PROCESS_ESTIMATE_LINE_HEIGHT)
        public float processEstimateLineHeight;

        @SerializedName(FIELD_PROCESS_NEED_LINE_HEIGHT)
        public float processNeedLineHeight;

        @SerializedName(FIELD_PROCESS_COMPLETE_LINE_HEIGHT)
        public float processCompleteLineHeight;

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
        int oddPositionBackgroundColor;

        @SerializedName(FIELD_EVEN_POSITION_BACKGROUND_COLOR)
        public @ColorInt
        int evenPositionBackgroundColor;

        @SerializedName(FIELD_START_WORK_LINE_COLOR)
        public @ColorInt
        int startWorkLineColor;

        @SerializedName(FIELD_COMPLETE_WORK_LINE_COLOR)
        public @ColorInt
        int completeWorkLineColor;

        @SerializedName(FIELD_TODAY_LINE_COLOR)
        public @ColorInt
        int todayLineColor;

        @SerializedName(FIELD_PROCESS_ESTIMATE_LINE_COLOR)
        public @ColorInt
        int processEstimateLineColor;

        @SerializedName(FIELD_PROCESS_NEED_LINE_COLOR)
        public @ColorInt
        int processNeedLineColor;

        @SerializedName(FIELD_PROCESS_COMPLETE_LINE_COLOR)
        public @ColorInt
        int processCompleteLineColor;

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

        public static Option getDefaultOption() {
            Option defaultOption = new Option();
            // TODO write default value
            return defaultOption;
        }

    }

    private static class ProcessChartPainter extends AbsProcessChartPainter {

        private static final float[] DASH_PARAMS = new float[]{6, 4};

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

            int size = shutdownMessages.size();
            for (int i = 0; i < size; i++) {

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
            drawBorder(canvas, paint, 0f, 0f, mOption.leftColumnWidth, mOption.chartHeaderHeight, mOption.headerLineColor);
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

        private void drawBorder(Canvas canvas, Paint paint, float left, float top, float right, float bottom, @ColorInt int color) {
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
            drawBorder(canvas, paint, mOption.width - mOption.rightColumnWidth, 0, mOption.width, mOption.chartHeaderHeight, mOption.headerLineColor);
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


        @Override
        void drawLeftColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process) {

        }

        @Override
        void drawRightColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process) {
            float startY = mOption.chartHeaderHeight + mOption.processItemHeight * position;
            float endY = startY + mOption.processItemHeight;
            float middleX = mOption.width - mOption.rightColumnWidth * 0.5f;
            float middleY = startY + mOption.processItemHeight * 0.5f;
            float offsetX = mOption.rightColumnWidth * 0.25f;
            // border
            setLinePaintStyle(paint, mOption.lineColor, mOption.lineWidth);
            drawBorder(canvas, paint, mOption.width - mOption.rightColumnWidth, startY, mOption.width, endY, mOption.lineColor);
            // split line
            drawVerticalLine(canvas, paint, startY, endY, middleX);
            // text
            setTextPaintStyle(paint, Paint.Align.CENTER, mOption.processNeedTextColor, mOption.processNeedTextSize);
            CanvasUtil.drawText(canvas, paint, "" + process.getNeedCompletePercent() , middleX - offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
            paint.setColor(mOption.processCompleteTextColor);
            paint.setTextSize(mOption.processCompleteTextSize);
            CanvasUtil.drawText(canvas, paint, "" + process.getProcessAlreadyCompletePercent(), middleX + offsetX, middleY, CanvasUtil.BASELINE_MODE_CENTER);
        }

        @Override
        void drawTimeLine(Canvas canvas, Paint paint, Project project) {

        }

        @Override
        void drawProcessContent(Canvas canvas, Paint paint, int position, ProcessWrapper process) {

        }

        @Override
        void drawProcessBackground(Canvas canvas, Paint paint, int position, ProcessWrapper process) {

        }

        private void setDashLinePaintStyle(Paint paint, @ColorInt int lineColor, float lineWidth) {
            paint.setPathEffect(mDashPathEffect);
            setLinePaintStyle(paint, lineColor, lineWidth);
        }

        @Override
        void drawStartWorkLine(Canvas canvas, Paint paint, Project project) {
            // FIXME 每次判断反而更慢，直接绘制更快
            canvas.save();
            canvas.translate(mOption.leftColumnWidth + (-mTouchHelper.getScrollX()), mOption.chartHeaderHeight);
            setDashLinePaintStyle(paint, mOption.startWorkLineColor, mOption.lineWidth);
            canvas.drawLine(0, 0, 0, mOption.processListHeight, paint);

            canvas.restore();
        }

        @Override
        void drawCompleteWorkLine(Canvas canvas, Paint paint, Project project) {
            canvas.save();
            canvas.translate(mOption.leftColumnWidth + (-mTouchHelper.getScrollX() + DateUtil.getDateDiff(mTouchHelper.projectBeginTime, mTouchHelper.projectEndTime)), mOption.chartHeaderHeight);
            setDashLinePaintStyle(paint, mOption.startWorkLineColor, mOption.lineWidth);
            canvas.drawLine(0, 0, 0, mOption.processListHeight, paint);
            paint.reset();
            canvas.restore();
        }

        @Override
        void drawTodayLine(Canvas canvas, Paint paint, Project project) {
            // FIXME today的更新滞后问题
            canvas.save();
            canvas.translate(mOption.leftColumnWidth + (-mTouchHelper.getScrollX() + DateUtil.getDateDiff(mTouchHelper.projectBeginTime, today)), mOption.chartHeaderHeight);
            setDashLinePaintStyle(paint, mOption.startWorkLineColor, mOption.lineWidth);
            canvas.drawLine(0, 0, 0, mOption.processListHeight, paint);
            paint.reset();
            canvas.restore();
        }

        @Override
        public void drawProcessList(Canvas canvas, Paint paint, Project project, List<ProcessWrapper> processWrappers) {
            int currPosition = mTouchHelper.getCurrentPosition();
            // 1.先根据position绘制背景


        }

    }

    private static class TouchHelper {

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

        private int sumDayInProject;

        private int processListSize;

        private float maxScrollX;

        private float maxScrollY;

        private float minScrollX;

        private float minScrollY;

        private Scroller mScroller;

        private Date projectBeginTime;

        private Date projectEndTime;

        private Option mOption;

        public TouchHelper(Context context, Option option) {
            mCalendar = Calendar.getInstance();
            mScroller = new Scroller(context);
            mOption = option;
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

        public void scrollBy(float scrollX, float scrollY) {
            scrollTo(this.scrollX + scrollX, this.scrollY + scrollY);
        }

        public void scrollTo(float scrollX, float scrollY) {
            setScrollX(scrollX);
            setScrollY(scrollY);
        }

        public void refreshData(Project project) {
            sumDayInProject = DateUtil.getDateDiff(project.getEndTime(), project.getBeginTime());
            processListSize = project.getProcesses().size();
            minScrollX = 0;
            maxScrollX = sumDayInProject * mOption.timeBarDayWidth - mOption.timeBarWidth;
            minScrollY = 0;
            maxScrollY = processListSize * mOption.processItemHeight - mOption.processListHeight;
            projectBeginTime = DateUtil.getDayFromDate(project.getBeginTime());
            projectEndTime = DateUtil.getDayFromDate(project.getEndTime());
        }

        /**
         * 根据scrollX 获取对应的 所在工程中天数
         *
         * @param scrollX x轴滚动值
         * @return 所在工程天数
         */
        public int getDayInProjectWithScrollX(float scrollX) {
            return (int) getValueInRange(scrollX, 0, sumDayInProject);
        }


        /**
         * 根据scrollY 获取对应的 所在processList中的index
         *
         * @param scrollY y轴滚动值
         * @return index in process list
         */
        public int getPositionInProcessListWithScrollY(float scrollY) {
            return (int) getValueInRange(scrollY, 0, processListSize);
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
            return getValueInRange(dayInProject * mOption.timeBarDayWidth - xInTimeBar, minScrollX, maxScrollX);
        }

        /**
         * 根据position和在工序列表上的y的值来获取scrollY
         *
         * @param positionInProcessList position
         * @param yInProcessListView    在工序列表上的y的值(以工序列表绘制起点Y轴坐标为起点0)
         * @return scrollY minScrollY <= result <= maxScrollY
         */
        public float getScrollY(int positionInProcessList, float yInProcessListView) {
            return getValueInRange(positionInProcessList * mOption.processListHeight - yInProcessListView, minScrollY, maxScrollY);
        }


        public float getScrollX() {
            return scrollX;
        }

        public void setScrollX(float scrollX) {
            this.scrollX = scrollX;
            this.offsetX = this.scrollX - (long) this.scrollX;
        }

        public float getScrollY() {
            return scrollY;
        }

        public void setScrollY(float scrollY) {
            this.scrollY = scrollY;
            this.offsetY = this.scrollY - (long) this.scrollY;
        }

        public float getOffsetX() {
            return offsetX;
        }


        public float getOffsetY() {
            return offsetY;
        }

    }

}

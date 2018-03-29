package com.baby.project.projectbaby.process.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
        private static final String FIELD_RIGHT_COLUMN_HEADER_TEXT = "right_column_header_text";
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

        @SerializedName(FIELD_RIGHT_COLUMN_HEADER_TEXT)
        public String rightColumnHeaderText = "完成情况";

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
        public float leftColumnWidth = 60;

        @SerializedName(FIELD_RIGHT_COLUMN_WIDTH)
        public float rightColumnWidth;

        @SerializedName(FIELD_CHART_HEADER_HEIGHT)
        public float chartHeaderHeight = 50;
        /**
         * process list
         */
        public float processListHeight;

        @SerializedName(FIELD_PROCESS_ITEM_HEIGHT)
        public float processItemHeight;

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
        int processCompleteTextColor;


        @SerializedName(FIELD_PROCESS_NEED_TEXT_COLOR)
        public @ColorInt
        int processNeedTextColor;

        /**
         * extra
         */
        @SerializedName(FIELD_LINE_WIDTH)
        public float lineWidth = 1.2f;

        @SerializedName(FIELD_HEADER_TEXT_SIZE)
        public float headerTextSize = 13.0f;

        @SerializedName(FIELD_PROCESS_COMPLETE_TEXT_SIZE)
        public float processCompleteTextSize;

        @SerializedName(FIELD_PROCESS_NEED_TEXT_SIZE)
        public float processNeedTextSize;

        public static Option getDefaultOption() {
            Option defaultOption = new Option();
            // TODO write default value
            return defaultOption;
        }

    }

    private static class ProcessChartPainter extends AbsProcessChartPainter {

        private TouchHelper mTouchHelper;

        private Option mOption;

        public ProcessChartPainter(TouchHelper touchHelper, Option option) {
            mTouchHelper = touchHelper;
            mOption = option;
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
            paint.setColor(mOption.headerBackgroundColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0f, 0f, mOption.width, mOption.chartHeaderHeight, paint);
        }

        @Override
        void drawLeftHeader(Canvas canvas, Paint paint) {
            // border
            drawBorder(canvas, paint, 0f, 0f, mOption.leftColumnWidth, mOption.chartHeaderHeight,mOption.headerLineColor);
            // split line
            canvas.drawLine(0f, 0f, mOption.leftColumnWidth, mOption.chartHeaderHeight, paint);
            // bottom text
            float middleHeight = mOption.chartHeaderHeight * 0.5f;
            float middleWidth = mOption.leftColumnWidth * 0.5f;
            float bottomTextX = middleWidth * 0.5f;
            float topTextX = mOption.leftColumnWidth - middleWidth * 0.5f;
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(mOption.headerTextColor);
            paint.setTextSize(mOption.headerTextSize);
            CanvasUtil.drawText(canvas, paint, mOption.leftColumnHeaderBottomText, bottomTextX, middleHeight, CanvasUtil.BASELINE_MODE_BOTTOM);
            CanvasUtil.drawText(canvas, paint, mOption.liftColumnHeaderTopText, topTextX, middleHeight, CanvasUtil.BASELINE_MODE_TOP);
        }


        private void drawBorder(Canvas canvas, Paint paint, float left, float top, float right, float bottom,@ColorInt  int color) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(mOption.lineWidth);
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

        }

        @Override
        void drawLeftColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process) {

        }

        @Override
        void drawRightColumn(Canvas canvas, Paint paint, int position, ProcessWrapper process) {

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

        @Override
        void drawStartWorkLine(Canvas canvas, Paint paint, Project project) {

        }

        @Override
        void drawCompleteWorkLine(Canvas canvas, Paint paint, Project project) {

        }

        @Override
        void drawTodayLine(Canvas canvas, Paint paint, Project project) {

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
            return DateUtil.convertDayToDate(projectBeginTime, projectEndTime, getDayInProjectWithScrollX(scrollX));
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

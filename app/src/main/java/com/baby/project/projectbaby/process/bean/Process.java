package com.baby.project.projectbaby.process.bean;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * 进度实体类
 * objectId，updateTime，createdTime，acl继承自AvObject
 * Created by yosemite on 2018/3/17.
 */

public class Process {

    private static final String FIELD_PROCESS_NAME = "process_name";
    private static final String FIELD_PROCESS_COST = "process_cost";
    private static final String FIELD_PROCESS_BEGIN_TIME = "process_begin_time";
    private static final String FIELD_PROCESS_END_TIME = "process_end_time";
    private static final String FIELD_PROCESS_ALREADY_COMPLETE_PERCENT = "p_a_c_percent";
    private static final String FIELD_PROCESS_USE_DAYS = "process_use_days";
    private static final String FIELD_PROCESS_DELAY_TIMES = "process_delay_times";


    @SerializedName(FIELD_PROCESS_NAME)
    private String processName;             // 工序信息

    @SerializedName(FIELD_PROCESS_COST)
    private float processCost;              // 工序造价

    @SerializedName(FIELD_PROCESS_BEGIN_TIME)
    private Date processBeginTime;          // 工序开工时间

    @SerializedName(FIELD_PROCESS_END_TIME)
    private Date processEndTime;            // 工序完工时间(也可能是预计)

    @SerializedName(FIELD_PROCESS_ALREADY_COMPLETE_PERCENT)
    private float processAlreadyCompletePercent;// 工序已经完成百分比(占总量的)

    @SerializedName(FIELD_PROCESS_USE_DAYS)
    private int processUseDays;             // 日历天数

    @SerializedName(FIELD_PROCESS_DELAY_TIMES)
    private List<Project.ShutdownMessage> processShutdownTimes;  // 工序停工开始时间

    public List<Project.ShutdownMessage> getProcessShutdownTimes() {
        return processShutdownTimes;
    }

    public void setProcessShutdownTimes(List<Project.ShutdownMessage> processShutdownTimes) {
        this.processShutdownTimes = processShutdownTimes;
    }

    public int getProcessUseDays() {
        return processUseDays;
    }

    public void setProcessUseDays(int processUseDays) {
        this.processUseDays = processUseDays;
    }


    public Date getProcessBeginTime() {
        return processBeginTime;
    }

    public void setProcessBeginTime(Date processBeginTime) {
        this.processBeginTime = processBeginTime;
    }

    public Date getProcessEndTime() {
        return processEndTime;
    }

    public void setProcessEndTime(Date processEndTime) {
        this.processEndTime = processEndTime;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public float getProcessCost() {
        return processCost;
    }

    public void setProcessCost(float processCost) {
        this.processCost = processCost;
    }

    public float getProcessAlreadyCompletePercent() {
        return processAlreadyCompletePercent;
    }

    public void setProcessAlreadyCompletePercent(float processAlreadyCompletePercent) {
        this.processAlreadyCompletePercent = processAlreadyCompletePercent;
    }
}

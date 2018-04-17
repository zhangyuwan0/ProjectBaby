package com.baby.project.projectbaby.process.bean;

import com.baby.project.projectbaby.sign.bean.User;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * 工程实体类
 * objectId，updateTime，createdTime，acl继承自AvObject
 * Created by yosemite on 2018/3/17.
 */

public class Project {

    private static final String FIELD_PROJECT_NAME = "project_name";
    private static final String FIELD_PROJECT_DESCRIBE = "project_describe";
    private static final String FIELD_PROJECT_LOGO = "project_logo";
    private static final String FIELD_PROJECT_VERSION = "project_version";
    private static final String FIELD_PROJECT_EXTRA = "project_extra";
    private static final String FIELD_PROJECT_BEGIN_TIME = "project_begin_time";
    private static final String FIELD_PROJECT_END_TIME = "project_end_time";
    private static final String FIELD_PROJECT_SHUTDOWN_DETAIL = "project_shutdown_detail";
    private static final String FIELD_PROJECT_PROCESSES = "project_processes";
    private static final String FIELD_PROJECT_TODAY = "project_today";


    @SerializedName(FIELD_PROJECT_NAME)
    private String projectName;     // 工程名称

    @SerializedName(FIELD_PROJECT_LOGO)
    private String logo;            // 工程logo

    @SerializedName(FIELD_PROJECT_DESCRIBE)
    private String describe;         // 工程描述

    @SerializedName(FIELD_PROJECT_BEGIN_TIME)
    private Date beginTime;         // 工程开工时间

    @SerializedName(FIELD_PROJECT_END_TIME)
    private Date endTime;           // 工程完工时间

    @SerializedName(FIELD_PROJECT_SHUTDOWN_DETAIL)
    private List<ShutdownMessage> shutdownMessages;// 工程停工表

    @SerializedName(FIELD_PROJECT_PROCESSES)
    private List<Process> processes;// 工程工序表

    @SerializedName(FIELD_PROJECT_EXTRA)
    private ExtraMessage extraMessage;// 工程扩展信息

    // TODO 考虑如何接受参数，参数类型，及转换
    @SerializedName(FIELD_PROJECT_TODAY)
    private Date today = new Date();             // 服务器时间(今天)

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<ShutdownMessage> getShutdownMessages() {
        return shutdownMessages;
    }

    public void setShutdownMessages(List<ShutdownMessage> shutdownMessages) {
        this.shutdownMessages = shutdownMessages;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    public ExtraMessage getExtraMessage() {
        return extraMessage;
    }

    public void setExtraMessage(ExtraMessage extraMessage) {
        this.extraMessage = extraMessage;
    }

    public static class ShutdownMessage {
        private static final String FIELD_BEGIN_TIME = "begin_time";
        private static final String FIELD_END_TIME = "end_time";
        private static final String FIELD_REASON = "reason";
        private static final String FIELD_REMARK = "remark";
        private static final String FIELD_OPERATER = "operater";

        @SerializedName(FIELD_BEGIN_TIME)
        private Date beginTime;   // 停工开始时间

        @SerializedName(FIELD_END_TIME)
        private Date endTime;     // 停工结束时间

        @SerializedName(FIELD_REASON)
        private String reason;      // 停工原因

        @SerializedName(FIELD_REMARK)
        private String remark;      // 停工备注

        @SerializedName(FIELD_OPERATER)
        private User user;          // 操作人

        public Date getBeginTime() {
            return beginTime;
        }

        public void setBeginTime(Date beginTime) {
            this.beginTime = beginTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    /**
     * 工程的其他信息bean类
     */
    public static class ExtraMessage {
        private static final String FIELD_PROJECT_EXTRA_ALREADY_COMPLETE_PRECENT = "p_a_c_percent";
        private static final String FIELD_PROJECT_PRICE = "p_price";

        @SerializedName(FIELD_PROJECT_EXTRA_ALREADY_COMPLETE_PRECENT)
        private Float projectAlreadyCompletePercent;    // 工程已完成百分比

        @SerializedName(FIELD_PROJECT_PRICE)
        private Float projectPrice;                     // 工程造价

        public Float getProjectAlreadyCompletePercent() {
            return projectAlreadyCompletePercent;
        }

        public void setProjectAlreadyCompletePercent(Float projectAlreadyCompletePercent) {
            this.projectAlreadyCompletePercent = projectAlreadyCompletePercent;
        }
    }

}

package com.baby.project.projectbaby.process.bean;

import android.util.Log;

import com.baby.project.projectbaby.process.utils.ArrayUtil;
import com.baby.project.projectbaby.process.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 工序适配类 用于方便绘制数据
 * Created by yosemite on 2018/3/17.
 */

public class ProcessWrapper {

    private long beginDay;                    // 工序开始时所在工程天数

    private long endDay;                      // 工序结束时所在工程天数

    private float unCompletePercent;         // 未完成百分比

    private float unCompletePercentDays;     // 未完成百分比所占天数 + 延误天数

    private float needCompletePercent;       // 需要完成百分比

    private float needCompletePercentDays;   // 需要完成百分比所占天数 + 延误天数

    private float alreadyCompletePercentDays;// 已经完成百分比所占天数 + 延误天数

    private int shutdownDays;                // 工序停工天数

    private int shutdownDaysBeforeToday;     // 在今天之前的停工天数

    private Process process;                 // 工序

    public static List<ProcessWrapper> convertProcessList(Project project) {
        if (project == null) {
            return null;
        }

        List<ProcessWrapper> wrappers = new ArrayList<>();
        List<Process> processes = project.getProcesses();
        if (processes == null || processes.size() == 0) {
            return wrappers;
        }

        int size = processes.size();
        Date today = DateUtil.getDayFromDate(project.getToday());
        // 让今天不超出限制
        today.setTime(DateUtil.getDateWithTime(project.getBeginTime(), project.getEndTime(), today));
        Date projectBeginTime = DateUtil.getDayFromDate(project.getBeginTime());
        Date projectEndTime = DateUtil.getDayFromDate(project.getEndTime());
        Date tempDate = new Date();
        long projectSumDays = DateUtil.getDateDiff(projectBeginTime, projectEndTime);
        for (int i = 0; i < size; i++) {
            Process process = processes.get(i);
            Date processBeginDate = DateUtil.getDayFromDate(process.getProcessBeginTime());
            Date processEndDate = DateUtil.getDayFromDate(process.getProcessEndTime());
            ProcessWrapper wrapper = new ProcessWrapper();
            wrapper.setProcess(process);
            wrapper.setBeginDay(DateUtil.getDateDiff(processBeginDate, projectBeginTime));
            wrapper.setEndDay(DateUtil.getDateDiff(processEndDate, projectBeginTime));
            setShutdownDaysAndBeforeTodayDays(wrapper, process, today);
            wrapper.setUnCompletePercent(1.0f - process.getProcessAlreadyCompletePercent());
            // 时间条 而不是所用时间
            wrapper.setUnCompletePercentDays(wrapper.endDay - wrapper.shutdownDays + process.getProcessUseDays() * wrapper.unCompletePercent);
            // 因为今天是预计完成 所以要+1天
            tempDate.setTime(DateUtil.getDateWithTime(processBeginDate, processEndDate, today.getTime() + DateUtil.DAY_MILLS));
            long todayDays = DateUtil.getDateDiff(processBeginDate, tempDate) - wrapper.shutdownDaysBeforeToday;
            wrapper.setNeedCompletePercent(todayDays / (float) (process.getProcessUseDays()));
            wrapper.setNeedCompletePercentDays(wrapper.beginDay + wrapper.shutdownDaysBeforeToday + DateUtil.convertDateToDay(process.getProcessBeginTime(), process.getProcessEndTime(), today));
            wrapper.setAlreadyCompletePercentDays(wrapper.beginDay + wrapper.shutdownDaysBeforeToday + process.getProcessUseDays() * process.getProcessAlreadyCompletePercent());
            wrappers.add(wrapper);
        }
        return wrappers;
    }

    private static void setShutdownDaysAndBeforeTodayDays(ProcessWrapper wrapper, Process process, Date today) {
        int shutdownDays = 0;
        Date processEndTime = DateUtil.getDayFromDate(process.getProcessEndTime());
        List<Project.ShutdownMessage> shutdownMessages = process.getProcessShutdownTimes();
        int shutdownDaysBeforeToday = 0;

        if (!ArrayUtil.isEmpty(shutdownMessages)) {
            for (Project.ShutdownMessage shutdownMessage : shutdownMessages) {
                Date beginTime = DateUtil.getDayFromDate(shutdownMessage.getBeginTime());
                Date endTime = DateUtil.getDayFromDate(shutdownMessage.getEndTime());
                // 停工开始时间必定在工序开工之后
                if (endTime.after(processEndTime)) {
                    shutdownDays += DateUtil.getDateDiff(beginTime, processEndTime);
                } else {
                    shutdownDays += DateUtil.getDateDiff(beginTime, endTime);
                }
                // 今天在停工之后才加
                if (beginTime.before(today)) {
                    switch (endTime.compareTo(today)) {
                        case 0:
                        case -1:
                            shutdownDaysBeforeToday += DateUtil.getDateDiff(beginTime, today);
                            break;
                        default:
                            shutdownDaysBeforeToday += DateUtil.getDateDiff(beginTime, endTime);
                            break;
                    }
                }
            }
        }
        wrapper.setShutdownDays(shutdownDays);
        wrapper.setShutdownDaysBeforeToday(shutdownDaysBeforeToday);
    }

    public int getShutdownDaysBeforeToday() {
        return shutdownDaysBeforeToday;
    }

    public void setShutdownDaysBeforeToday(int shutdownDaysBeforeToday) {
        this.shutdownDaysBeforeToday = shutdownDaysBeforeToday;
    }

    public float getAlreadyCompletePercentDays() {
        return alreadyCompletePercentDays;
    }

    public void setAlreadyCompletePercentDays(float alreadyCompletePercentDays) {
        this.alreadyCompletePercentDays = alreadyCompletePercentDays;
    }

    public int getShutdownDays() {
        return shutdownDays;
    }

    public void setShutdownDays(int shutdownDays) {
        this.shutdownDays = shutdownDays;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public long getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(long beginDay) {
        this.beginDay = beginDay;
    }

    public long getEndDay() {
        return endDay;
    }

    public void setEndDay(long endDay) {
        this.endDay = endDay;
    }

    public float getUnCompletePercent() {
        return unCompletePercent;
    }

    public void setUnCompletePercent(float unCompletePercent) {
        this.unCompletePercent = unCompletePercent;
    }

    public float getUnCompletePercentDays() {
        return unCompletePercentDays;
    }

    public void setUnCompletePercentDays(float unCompletePercentDays) {
        this.unCompletePercentDays = unCompletePercentDays;
    }

    public float getNeedCompletePercent() {
        return needCompletePercent;
    }

    public void setNeedCompletePercent(float needCompletePercent) {
        this.needCompletePercent = needCompletePercent;
    }

    public float getNeedCompletePercentDays() {
        return needCompletePercentDays;
    }

    public void setNeedCompletePercentDays(float needCompletePercentDays) {
        this.needCompletePercentDays = needCompletePercentDays;
    }

}

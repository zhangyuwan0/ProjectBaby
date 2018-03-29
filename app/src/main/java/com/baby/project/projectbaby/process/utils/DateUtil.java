package com.baby.project.projectbaby.process.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Date 工具类
 * Created by yosemite on 2018/3/18.
 */

public class DateUtil {

    private static final long DAY_MILLS = 1000 * 60 * 60 * 24L;
    private static Calendar sCalendar = Calendar.getInstance();

    /**
     * 将日期转为在工程中天数
     *
     * @param beginDate  开始日期
     * @param endDate    结束日期
     * @param targetDate 待转换日期
     * @return 如果在范围内返回正常天数，小于开始日期返回0，大于结束日期返回 结束日期-开始日期 天数
     */
    public static int convertDateToDay(Date beginDate, Date endDate, Date targetDate) {
        int result;

        if (targetDate.after(endDate)) {
            result = getDateDiff(endDate, beginDate);
        } else if (targetDate.before(beginDate)) {
            result = 0;
        } else {
            result = getDateDiff(targetDate, beginDate);
        }
        return result;
    }

    /**
     * 获取两日期相差天数(绝对值)
     *
     * @param d1 date one
     * @param d2 date two
     * @return 相差天数的绝对值
     */
    public static int getDateDiff(Date d1, Date d2) {
        return (int) Math.abs((d1.getTime() - d2.getTime()) / DAY_MILLS);
    }

    /**
     * 从日期中获取天数(剥离时分秒)
     *
     * @param date need convert date
     * @return date(剥离时分秒)
     */
    public static Date getDayFromDate(Date date) {
        return new Date(date.getTime() / DAY_MILLS * DAY_MILLS);
    }

    /**
     * 将所在工程中天数转为日期
     *
     * @param beginDate    工程开始时间
     * @param endDate      工程结束时间
     * @param dayInProject 所在工程中天数
     * @return dayInProject所在工程中日期(long值)
     */
    public static long convertDayToDate(Date beginDate, Date endDate, int dayInProject) {
        int diff = getDateDiff(endDate, beginDate);
        if (dayInProject > diff) {
            return endDate.getTime();
        } else {
            return beginDate.getTime() + DAY_MILLS * dayInProject;
        }
    }

    /**
     * 获取year年一月一日的time
     *
     * @param beginDate 工程开始时间
     * @param endDate   工程结束时间
     * @param year      年份
     * @return 开工年份 <= result <= 完工年份
     */
    public static long getDateWithYear(Date beginDate, Date endDate, int year) {
        return getDateWithYearAndMonth(beginDate, endDate, year, 1);
    }

    /**
     * 获取year年month月一日的time
     *
     * @param beginDate 工程开始时间
     * @param endDate   工程结束时间
     * @param year      年份
     * @param month     月份
     * @return 开工年/月/日 <= result <= 完工年/月/日
     */
    public static long getDateWithYearAndMonth(Date beginDate, Date endDate, int year, int month) {
        month = Math.max(1, Math.min(month, 12));
        sCalendar.setTime(beginDate);
        int beginYear = sCalendar.get(Calendar.YEAR);
        sCalendar.setTime(endDate);
        int endYear = sCalendar.get(Calendar.YEAR);
        year = Math.max(beginYear, Math.min(year, endYear));
        sCalendar.set(year, month, 1);
        return getDateWithTime(beginDate, endDate, sCalendar.getTimeInMillis());
    }

    /**
     * 根据time 获取 date
     *
     * @param beginDate 工程开始时间
     * @param endDate   工程结束时间
     * @param time      时间(long)
     * @return 开工年/月/日 <= result <= 完工年/月/日
     */
    public static long getDateWithTime(Date beginDate, Date endDate, long time) {
        Date begin = getDayFromDate(beginDate);
        Date end = getDayFromDate(endDate);
        return Math.max(begin.getTime(), Math.min(time, end.getTime()));
    }

    /**
     * 根据time 获取 date
     *
     * @param beginDate 工程开始时间
     * @param endDate   工程结束时间
     * @param time      时间(Date)
     * @return 开工年/月/日 <= result <= 完工年/月/日
     */
    public static long getDateWithTime(Date beginDate, Date endDate, Date time) {
        Date target = getDayFromDate(time);
        return getDateWithTime(beginDate, endDate, target);
    }

}

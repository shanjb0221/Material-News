package com.example.myapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* REFERENCES
 * - [CSDN](https://blog.csdn.net/qq_34492495/article/details/89671496)
 */
public class TimeUtil {

    private static final long SECOND = 1000L;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;

    // 将传入时间与当前时间进行对比，是否今天\昨天\前天\同一年
    public static String format(Date date) {
        long time = date.getTime();
        long nowTime = System.currentTimeMillis();
        long delta = nowTime - time;

        if (delta < 0) return "将来";
        if (delta < 10 * SECOND) return "刚刚";
        if (delta < MINUTE) return delta / SECOND + "秒前";
        if (delta < HOUR) return delta / MINUTE + "分钟前";

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.setTime(new Date(nowTime));
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);

        boolean sameYear = dateCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR);
        String res = new SimpleDateFormat("HH:mm", Locale.CHINA).format(date);
        if (dateCalendar.after(todayCalendar)) { // 今天
            return res;
        } else {
            todayCalendar.add(Calendar.DATE, -1);
            if (dateCalendar.after(todayCalendar)) { // 昨天
                return "昨天 " + res;
            }
            todayCalendar.add(Calendar.DATE, -1);
            if (dateCalendar.after(todayCalendar)) { // 前天
                return "前天 " + res;
            }
        }

        if (sameYear)
            return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA).format(date);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(date);
    }
}


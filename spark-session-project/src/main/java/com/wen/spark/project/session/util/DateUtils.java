package com.wen.spark.project.session.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author : WChen129
 * @date : 2018-05-25
 */
public class DateUtils {
    public final static String DATE_FORMAT = "yyyy-MM-dd";
    public final static String yyyy_MM_dd_HHmmssSSS = "yyyy-MM-dd HH:mm:ss:SSS";
    public final static String yyyy_MM_dd_HHmmss = "yyyy-MM-dd HH:mm:ss";
    public final static String yyyy_MM_ddTHHmmss = "yyyy-MM-dd'T'HH:mm:ss";
    public static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 判断一个时间是否在另一个时间之前
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @return 判断结果
     */
    public static boolean before(String time1, String time2) {
        try {
            Date dateTime1 = TIME_FORMAT.parse(time1);
            Date dateTime2 = TIME_FORMAT.parse(time2);

            if(dateTime1.before(dateTime2)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断一个时间是否在另一个时间之后
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @return 判断结果
     */
    public static boolean after(String time1, String time2) {
        try {
            Date dateTime1 = TIME_FORMAT.parse(time1);
            Date dateTime2 = TIME_FORMAT.parse(time2);

            if(dateTime1.after(dateTime2)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算时间差值（单位为秒）
     * @param time1 时间1
     * @param time2 时间2
     * @return 差值
     */
    public static int minus(String time1, String time2) {
        try {
            Date datetime1 = TIME_FORMAT.parse(time1);
            Date datetime2 = TIME_FORMAT.parse(time2);

            long millisecond = datetime1.getTime() - datetime2.getTime();

            return Integer.valueOf(String.valueOf(millisecond / 1000));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取年月日和小时
     * @param datetime 时间（yyyy-MM-dd HH:mm:ss）
     * @return 结果
     */
    public static String getDateHour(String datetime) {
        String date = datetime.split(" ")[0];
        String hourMinuteSecond = datetime.split(" ")[1];
        String hour = hourMinuteSecond.split(":")[0];
        return date + "_" + hour;
    }





    /**
     * 格式化时间（yyyy-MM-dd HH:mm:ss）
     * @param date Date对象
     * @return 格式化后的时间
     */
    public static String formatTime(Date date) {
        return TIME_FORMAT.format(date);
    }

    /**
     * 解析时间字符串
     * @param time 时间字符串
     * @return
     */
    public static Date parseTime(String time) {
        try {
            return TIME_FORMAT.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String dateToString(Date dt) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(dt);
    }

    public static String dateToString(Date dt, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(dt);
    }


    public static Date stringToDate(String dateStr, String format) {
        return stringToDate(dateStr, format, Locale.getDefault());
    }
    public static Date stringToDate(String dateStr, String format, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, locale);
        Date s_date = null;
        try {
            s_date = (Date) sdf.parse(dateStr);
        } catch (ParseException e) {
            s_date = correctDate(dateStr, format);
        }
        return s_date;
    }
    private static Date correctDate(String dateStr, String format) {
        Map<String, Locale> set = new HashMap<String, Locale>();
        set.put(DATE_FORMAT, Locale.CHINA);
        set.put(yyyy_MM_dd_HHmmssSSS, Locale.CHINA);
        set.put(yyyy_MM_dd_HHmmss, Locale.CHINA);
        set.put(yyyy_MM_ddTHHmmss, Locale.CHINA);
        set.remove(format);
        for (Map.Entry<String, Locale> es : set.entrySet()) {
            SimpleDateFormat sdf = new SimpleDateFormat(es.getKey(),
                    es.getValue());

            try {
                Date s_date = sdf.parse(dateStr);
                return s_date;
            } catch (ParseException e) {
            }
        }
        return null;
    }
    public static Date getNowDate(){
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }
    public static long currentTimeMillis() {
        return  System.currentTimeMillis();
    }

    public static String fullZero(int time){
        if(time<10&&time>=0){
            return "0"+time;
        }else{
            return time+"";
        }

    }

}

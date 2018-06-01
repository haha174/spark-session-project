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

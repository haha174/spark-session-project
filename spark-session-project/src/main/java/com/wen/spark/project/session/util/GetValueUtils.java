package com.wen.spark.project.session.util;

import org.apache.commons.lang3.StringUtils;

public class GetValueUtils {
    /**
     * getString if null or eror   return defaultValue
     * @param value
     * @param defaultValue
     * @return
     */
    public static String getStringOrElse(Object value, String defaultValue) {
        String valueString = String.valueOf(value);
        if (StringUtils.isNotBlank(valueString)) {
            return valueString;
        } else {
            return defaultValue;
        }
    }
    /**
     * get String defaultValue is ""
     *
     * @param value
     * @return
     */
    public static String getString(Object value) {
        return getStringOrElse(value, "");
    }

    /**
     * get int default value is 0
     * @param value
     * @return
     */
    public static int getInteger(Object value) {
        return getIntegerOrElse(value, 0);
    }

    /**
     * getInteger if not int return default value
     * @param value
     * @param defaultValue
     * @return
     */
    public static int getIntegerOrElse(Object value, int defaultValue) {
        try {
            if (value == null) return defaultValue;
            return Integer.parseInt((String) value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static double getDoubleOrElse(Object value) {
        return getDoubleOrElse(value, 0);
    }
    /**
     * get double
     * @param value
     * @param defaultValue
     * @return
     */
    public static double getDoubleOrElse(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble((String) value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    /**
     * get long
     * @param value
     * @return
     */
    public static double getLong(Object value) {
        return getDoubleOrElse(value, 0L);
    }
    /**
     * get long
     * @param value
     * @param defaultValue
     * @return
     */
    public static long getDoubleOrElse(Object value, long defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Long.parseLong((String) value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

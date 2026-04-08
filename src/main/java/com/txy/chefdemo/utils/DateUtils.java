package com.txy.chefdemo.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String format(Long time, String pattern) {
        if (ObjectUtils.isEmpty(time)) {
            return "";
        }
        return new SimpleDateFormat(pattern).format(new Date(time));
    }
}
package com.txy.chefdemo.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认值工具类 - 用于DTO字段兜底
 * 确保返回给前端的数据不包含null值
 */
public class DefaultValueUtil {
    
    /**
     * Long类型兜底 - null转为0L
     */
    public static Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }
    
    /**
     * Integer类型兜底 - null转为0
     */
    public static Integer defaultInteger(Integer value) {
        return value != null ? value : 0;
    }
    
    /**
     * String类型兜底 - null或空字符串转为"-"
     */
    public static String defaultString(String value) {
        return (value != null && !value.isEmpty()) ? value : "-";
    }
    
    /**
     * List类型兜底 - null转为空列表
     */
    public static <T> List<T> defaultList(List<T> value) {
        return value != null ? value : new ArrayList<>();
    }
}

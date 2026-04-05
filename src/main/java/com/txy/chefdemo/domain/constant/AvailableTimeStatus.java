package com.txy.chefdemo.domain.constant;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum AvailableTimeStatus {
    AVAILABLE(1, "可预约"),
    EXPIRED(2, "已过期"),
    DELETED(3, "已删除");

    private final int code;
    private final String desc;

    AvailableTimeStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static AvailableTimeStatus getByCode(int code) {
        for (AvailableTimeStatus status : AvailableTimeStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public static List<Integer> getValidCodes() {
       return List.of(AVAILABLE.code, EXPIRED.code);
    }

    public static List<Integer> getNormalCodes() {
        return List.of(AVAILABLE.code);
    }
}

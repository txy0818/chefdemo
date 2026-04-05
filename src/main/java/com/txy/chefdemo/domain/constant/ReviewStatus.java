package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum ReviewStatus {
    NORMAL(1, "正常"),
    DELETED(2, "已删除");

    private final int code;
    private final String desc;

    ReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
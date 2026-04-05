package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum AuditStatus {
    PENDING(1, "待审核"),
    APPROVED(2, "通过"),
    REJECTED(3, "拒绝");

    private final int code;
    private final String desc;

    AuditStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }
    public static AuditStatus getByCode(int code) {
        for (AuditStatus value : AuditStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
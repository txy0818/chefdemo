package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum ReviewAuditStatus {
    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String desc;

    ReviewAuditStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ReviewAuditStatus getByCode(int code) {
        for (ReviewAuditStatus value : ReviewAuditStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum ReportStatus {
    PENDING(1, "待处理"),
    CONFIRMED(2, "举报属实"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String desc;

    ReportStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static ReportStatus getByCode(int code) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

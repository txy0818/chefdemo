package com.txy.chefdemo.domain.constant;

public enum ReportType {
    BAD_ATTITUDE(1, "服务态度恶劣"),
    INVALID_QUALIFICATION(2, "资质不符"),
    FOOD_SAFETY(3, "食品安全问题"),
    BREACH_OF_AGREEMENT(4, "未按约定提供服务"),
    OTHER(5, "其他问题");

    private final int code;
    private final String desc;

    ReportType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

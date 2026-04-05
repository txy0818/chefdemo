package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum PaymentType {
    WALLET(1, "余额"),
    ;

    private final int code;
    private final String desc;

    PaymentType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}

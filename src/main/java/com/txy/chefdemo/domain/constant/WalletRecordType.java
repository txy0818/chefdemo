package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum WalletRecordType {
    RECHARGE(1, "充值"),
    PAYMENT(2, "支付"),
    REFUND(3, "退款");

    private final int code;
    private final String desc;

    WalletRecordType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
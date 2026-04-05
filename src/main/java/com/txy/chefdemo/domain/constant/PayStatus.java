package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum PayStatus {
    UNPAID(1, "未支付"),
    PAID(2, "已支付"),
    FAILED(3, "支付失败"),
    REFUNDED(4, "已退款");

    private final int code;
    private final String desc;

    PayStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static PayStatus fromCode(Integer code) {
        if (code == null) return null;
        for (PayStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}

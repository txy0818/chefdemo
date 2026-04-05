package com.txy.chefdemo.domain.constant;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum OrderStatus {
    PENDING_PAYMENT(1, "待支付"),
    PENDING_ACCEPT(2, "待接单"),
    ACCEPTED(3, "已接单"),
    REJECTED(4, "已拒单"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatus fromCode(Integer code) {
        for (OrderStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static List<Integer> getValidCodes() {
        return List.of(PENDING_PAYMENT.code, PENDING_ACCEPT.code, ACCEPTED.code, COMPLETED.code);
    }
}

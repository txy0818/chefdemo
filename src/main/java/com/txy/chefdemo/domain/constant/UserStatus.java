package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum UserStatus {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结");

    private final int code;
    private final String desc;

    UserStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus getByCode(Integer status) {
        for (UserStatus value : values()) {
            if (value.code == status) {
                return value;
            }
        }
        return null;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}

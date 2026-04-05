package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum UserRole {
    ADMIN(1, "管理员"),
    CHEF(2, "厨师"),
    NORMAL_USER(3, "普通用户");

    private final int code;
    private final String desc;

    UserRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static UserRole getByCode(int code) {
        for (UserRole value : UserRole.values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
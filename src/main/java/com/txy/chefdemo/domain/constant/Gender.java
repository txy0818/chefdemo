package com.txy.chefdemo.domain.constant;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public enum Gender {
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static Gender getByCode(int code) {
        for (Gender gender : Gender.values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return null;
    }
}
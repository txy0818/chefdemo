package com.txy.chefdemo.domain.constant;

public enum NotificationReadStatus {
    UNREAD(1, "未读"),
    READ(2, "已读");

    private final int code;
    private final String desc;

    NotificationReadStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}

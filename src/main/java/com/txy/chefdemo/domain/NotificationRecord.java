package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecord {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer readStatus;
    private Long createTime;
    private Long updateTime;
}

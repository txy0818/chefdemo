package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.NotificationRecord;

import java.util.List;

public interface NotificationRecordService {
    Long insert(NotificationRecord record);
    NotificationRecord queryById(Long id);
    List<NotificationRecord> queryByUserId(Long userId);
    List<NotificationRecord> queryPageByUserId(Long userId, Boolean unreadOnly, Long offset, Long size);
    Integer queryCountByUserId(Long userId, Boolean unreadOnly);
    Integer updateById(NotificationRecord record);
}

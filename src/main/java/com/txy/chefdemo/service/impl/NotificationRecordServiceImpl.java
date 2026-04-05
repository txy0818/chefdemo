package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.mapper.NotificationRecordMapper;
import com.txy.chefdemo.service.NotificationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRecordServiceImpl implements NotificationRecordService {

    @Autowired
    private NotificationRecordMapper notificationRecordMapper;

    @Override
    public Long insert(NotificationRecord record) {
        return notificationRecordMapper.insert(record);
    }

    @Override
    public NotificationRecord queryById(Long id) {
        return notificationRecordMapper.queryById(id);
    }

    @Override
    public List<NotificationRecord> queryByUserId(Long userId) {
        return notificationRecordMapper.queryByUserId(userId);
    }


    @Override
    public List<NotificationRecord> queryPageByUserId(Long userId, Boolean unreadOnly, Long offset, Long size) {
        return notificationRecordMapper.queryPageByUserId(userId, unreadOnly, offset, size);
    }

    @Override
    public Integer queryCountByUserId(Long userId, Boolean unreadOnly) {
        return notificationRecordMapper.queryCountByUserId(userId, unreadOnly);
    }

    @Override
    public Integer updateById(NotificationRecord record) {
        return notificationRecordMapper.updateById(record);
    }
}

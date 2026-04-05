package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.UserStatusRecord;
import com.txy.chefdemo.mapper.UserStatusRecordMapper;
import com.txy.chefdemo.service.UserStatusRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class UserStatusRecordServiceImpl implements UserStatusRecordService {
    @Autowired
    private UserStatusRecordMapper userStatusRecordMapper;

    @Override
    public Long insert(UserStatusRecord record) {
        return userStatusRecordMapper.insert(record);
    }

    @Override
    public List<UserStatusRecord> queryByUserId(Long userId) {
        return userStatusRecordMapper.queryByUserId(userId);
    }
}

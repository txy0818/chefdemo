package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.UserStatusRecord;
import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface UserStatusRecordService {
    Long insert(UserStatusRecord record);

    List<UserStatusRecord> queryByUserId(Long userId);
}

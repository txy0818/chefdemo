package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.mapper.ChefAuditRecordMapper;
import com.txy.chefdemo.service.ChefAuditRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ChefAuditRecordServiceImpl implements ChefAuditRecordService {
    @Autowired
    private ChefAuditRecordMapper chefAuditRecordMapper;

    @Override
    public Long insert(ChefAuditRecord record) {
        return chefAuditRecordMapper.insert(record);
    }

    @Override
    public int updateById(List<ChefAuditRecord> record) {
        return chefAuditRecordMapper.updateById(record);
    }

    @Override
    public List<ChefAuditRecord> queryPendingRecord() {
        return chefAuditRecordMapper.queryPendingRecord();
    }

    @Override
    public ChefAuditRecord queryPendingRecordByChefUserId(Long chefUserId) {
        return chefAuditRecordMapper.queryPendingRecordByChefUserId(chefUserId);
    }
}

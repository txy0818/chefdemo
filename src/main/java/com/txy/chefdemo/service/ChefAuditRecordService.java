package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefAuditRecord;
import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ChefAuditRecordService {
    Long insert(ChefAuditRecord record);

    List<ChefAuditRecord> queryPendingRecord();

    ChefAuditRecord queryPendingRecordByChefUserId(Long chefUserId);

    long updateById(List<ChefAuditRecord> record);
}

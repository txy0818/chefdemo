package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.ChefAuditRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ChefAuditRecordMapper {
    Long insert(@Param("record") ChefAuditRecord record);

    List<ChefAuditRecord> queryPendingRecord();

    ChefAuditRecord queryPendingRecordByChefUserId(Long chefUserId);

    long updateById(@Param("records") List<ChefAuditRecord> record);
}

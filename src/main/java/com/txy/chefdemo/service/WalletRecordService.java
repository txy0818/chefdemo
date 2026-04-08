package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.WalletRecord;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface WalletRecordService {
    /** 新增钱包流水. */
    Long insert(WalletRecord record);
    /** 根据用户ID查询钱包流水. */
    List<WalletRecord> queryByUserId(Long userId);
    /** 根据用户ID分页查询钱包流水. */
    List<WalletRecord> queryByUserIdPage(Long userId, Long offset, Long size);
    /** 根据用户ID查询钱包流水总数. */
    int queryByUserIdCount(Long userId);
    /** 根据订单ID查询钱包流水. */
    List<WalletRecord> queryByOrderId(Long orderId);
}

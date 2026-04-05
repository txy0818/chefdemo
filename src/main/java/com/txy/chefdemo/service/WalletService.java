package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.Wallet;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface WalletService {
    /** 新增钱包. */
    Long upsert(Wallet wallet);
    /** 根据ID查询钱包. */
    Wallet queryById(Long id);
    /** 根据用户ID查询钱包. */
    Wallet queryByUserId(Long userId);
    /** 根据ID更新钱包. */
    Long updateById(Wallet wallet);
}

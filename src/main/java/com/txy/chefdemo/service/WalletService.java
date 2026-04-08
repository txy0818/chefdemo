package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.Wallet;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface WalletService {
    Long insert(Wallet wallet);

    int updateById(Wallet wallet);

    /**
     * 仅保留兜底使用，业务代码优先走 insert / updateById。
     */
    Long upsert(Wallet wallet);

    Wallet queryById(Long id);

    Wallet queryByUserId(Long userId);
}

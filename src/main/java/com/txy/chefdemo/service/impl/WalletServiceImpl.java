package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.Wallet;
import com.txy.chefdemo.mapper.WalletMapper;
import com.txy.chefdemo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class WalletServiceImpl implements WalletService {
    @Autowired
    private WalletMapper walletMapper;

    @Override
    public Long upsert(Wallet wallet) {
        return walletMapper.upsert(wallet);
    }

    @Override
    public Wallet queryById(Long id) {
        return walletMapper.queryById(id);
    }

    @Override
    public Wallet queryByUserId(Long userId) {
        return walletMapper.queryByUserId(userId);
    }

    @Override
    public Long updateById(Wallet wallet) {
        return walletMapper.upsert(wallet);
    }
}

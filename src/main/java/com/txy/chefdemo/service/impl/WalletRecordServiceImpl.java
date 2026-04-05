package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.WalletRecord;
import com.txy.chefdemo.mapper.WalletRecordMapper;
import com.txy.chefdemo.service.WalletRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class WalletRecordServiceImpl implements WalletRecordService {
    @Autowired
    private WalletRecordMapper walletRecordMapper;

    @Override
    public Long insert(WalletRecord record) {
        return walletRecordMapper.insert(record);
    }

    @Override
    public List<WalletRecord> queryByUserId(Long userId) {
        return walletRecordMapper.queryByUserId(userId);
    }

    @Override
    public List<WalletRecord> queryByUserIdPage(Long userId, Long offset, Long size) {
        return walletRecordMapper.queryByUserIdPage(userId, offset, size);
    }

    @Override
    public int queryByUserIdCount(Long userId) {
        return walletRecordMapper.queryByUserIdCount(userId);
    }

    @Override
    public List<WalletRecord> queryByOrderId(Long orderId) {
        return walletRecordMapper.queryByOrderId(orderId);
    }

}

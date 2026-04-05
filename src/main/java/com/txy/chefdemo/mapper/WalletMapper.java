package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface WalletMapper {
    Long upsert(@Param("wallet") Wallet wallet);
    Wallet queryById(@Param("id") Long id);
    Wallet queryByUserId(@Param("userId") Long userId);
}

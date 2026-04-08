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
    Long insert(@Param("wallet") Wallet wallet);

    int updateById(@Param("wallet") Wallet wallet);

    /**
     * 基于 INSERT ... ON DUPLICATE KEY UPDATE 的兜底写法。
     * 注意：MySQL 在唯一键冲突走 UPDATE 时，也可能推进 AUTO_INCREMENT，导致“吃号”。
     */
    Long upsert(@Param("wallet") Wallet wallet);

    Wallet queryById(@Param("id") Long id);

    Wallet queryByUserId(@Param("userId") Long userId);
}

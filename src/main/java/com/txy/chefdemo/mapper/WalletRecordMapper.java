package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.WalletRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface WalletRecordMapper {
    Long insert(@Param("record") WalletRecord record);

    List<WalletRecord> queryByUserId(@Param("userId") Long userId);

    List<WalletRecord> queryByUserIdPage(@Param("userId") Long userId, @Param("offset") Long offset, @Param("size") Long size);

    int queryByUserIdCount(@Param("userId") Long userId);

    List<WalletRecord> queryByOrderId(@Param("reservationOrderId") Long orderId);
}

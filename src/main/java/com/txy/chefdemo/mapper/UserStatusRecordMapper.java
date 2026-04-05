package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.UserStatusRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface UserStatusRecordMapper {
    Long insert(@Param("record") UserStatusRecord record);
    List<UserStatusRecord> queryByUserId(@Param("userId") Long userId);
}

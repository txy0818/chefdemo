package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationRecordMapper {
    Long insert(@Param("record") NotificationRecord record);
    NotificationRecord queryById(@Param("id") Long id);
    List<NotificationRecord> queryByUserId(@Param("userId") Long userId);
    List<NotificationRecord> queryPageByUserId(@Param("userId") Long userId,
                                               @Param("unreadOnly") Boolean unreadOnly,
                                               @Param("offset") Long offset,
                                               @Param("size") Long size);
    Integer queryCountByUserId(@Param("userId") Long userId, @Param("unreadOnly") Boolean unreadOnly);
    Integer updateById(@Param("record") NotificationRecord record);
}

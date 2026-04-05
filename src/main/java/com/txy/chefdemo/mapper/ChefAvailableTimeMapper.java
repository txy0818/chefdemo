package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ChefAvailableTimeMapper {
    Long insert(@Param("time") ChefAvailableTime time);
    Integer updateById(@Param("time") ChefAvailableTime time);
    List<ChefAvailableTime> queryByCondition(@Param("searchBo") ChefAvailableTimeSearchBo searchBo);
    int queryChefListCnt(@Param("searchBo") ChefAvailableTimeSearchBo searchBo);
}

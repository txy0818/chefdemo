package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ChefAvailableTimeService {

    Long insert(ChefAvailableTime chefAvailableTime);

    Integer updateById(ChefAvailableTime time);

    List<ChefAvailableTime> queryByCondition(ChefAvailableTimeSearchBo searchBo);

    int queryChefListCnt(ChefAvailableTimeSearchBo searchBo);
}

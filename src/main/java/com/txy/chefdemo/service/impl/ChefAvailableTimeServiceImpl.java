package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.mapper.ChefAvailableTimeMapper;
import com.txy.chefdemo.service.ChefAvailableTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ChefAvailableTimeServiceImpl implements ChefAvailableTimeService {
    @Autowired
    private ChefAvailableTimeMapper chefAvailableTimeMapper;

    @Override
    public Long insert(ChefAvailableTime chefAvailableTime) {
        return chefAvailableTimeMapper.insert(chefAvailableTime);
    }

    @Override
    public int updateById(ChefAvailableTime time) {
        return chefAvailableTimeMapper.updateById(time);
    }

    @Override
    public List<ChefAvailableTime> queryByCondition(ChefAvailableTimeSearchBo searchBo) {
        return chefAvailableTimeMapper.queryByCondition(searchBo);
    }

    @Override
    public int queryChefListCnt(ChefAvailableTimeSearchBo searchBo) {
        return chefAvailableTimeMapper.queryChefListCnt(searchBo);
    }
}

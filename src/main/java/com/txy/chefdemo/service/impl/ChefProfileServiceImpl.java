package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.mapper.ChefProfileMapper;
import com.txy.chefdemo.service.ChefProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ChefProfileServiceImpl implements ChefProfileService {
    @Autowired
    private ChefProfileMapper chefProfileMapper;

    @Override
    public Long insert(ChefProfile chefProfile) {
        return chefProfileMapper.insert(chefProfile);
    }

    @Override
    public int updateById(ChefProfile chefProfile) {
        return chefProfileMapper.updateById(chefProfile);
    }

    @Override
    public Long upsert(ChefProfile chefProfile) {
        return chefProfileMapper.upsert(chefProfile);
    }

    @Override
    @Deprecated
    public Long batchUpsert(List<ChefProfile> chefProfiles) {
        return chefProfileMapper.batchUpsert(chefProfiles);
    }

    @Override
    public ChefProfile queryByUserId(Long userId) {
        return chefProfileMapper.queryByUserId(userId);
    }

    @Override
    public List<ChefProfile> queryChefListByCondition(ChefProfileSearchBo searchBo) {
        return chefProfileMapper.queryChefListByCondition(searchBo);
    }

    @Override
    public int queryChefListCnt(ChefProfileSearchBo searchBo) {
        return chefProfileMapper.queryChefListCnt(searchBo);
    }
}

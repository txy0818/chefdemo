package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ChefProfileService {
    /**
     * 新增厨师资料
     */
    Long upsert(ChefProfile chefProfile);

    Long batchUpsert(List<ChefProfile> chefProfiles);

    /**
     * 根据用户ID查询厨师资料
     */
    ChefProfile queryByUserId(Long userId);

    /** 条件查询厨师资料. */
    List<ChefProfile> queryChefListByCondition(ChefProfileSearchBo searchBo);

    int queryChefListCnt(ChefProfileSearchBo searchBo);
}

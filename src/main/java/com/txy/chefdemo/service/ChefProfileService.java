package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ChefProfileService {
    Long insert(ChefProfile chefProfile);

    int updateById(ChefProfile chefProfile);

    /**
     * 仅保留兜底使用，业务代码优先走 insert / updateById。
     */
    Long upsert(ChefProfile chefProfile);

    /**
     * 批量 upsert 同样只保留兜底使用。
     */
    @Deprecated
    Long batchUpsert(List<ChefProfile> chefProfiles);

    /**
     * 根据用户 ID 查询厨师资料。
     */
    ChefProfile queryByUserId(Long userId);

    /**
     * 按条件查询厨师资料。
     */
    List<ChefProfile> queryChefListByCondition(ChefProfileSearchBo searchBo);

    int queryChefListCnt(ChefProfileSearchBo searchBo);
}

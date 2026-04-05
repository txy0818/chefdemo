package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ChefProfileMapper {
    Long upsert(@Param("chefProfile") ChefProfile chefProfile);
    ChefProfile queryByUserId(@Param("userId") Long userId);

    List<ChefProfile> queryChefListByCondition(@Param("searchBo") ChefProfileSearchBo searchBo);

    int queryChefListCnt(@Param("searchBo") ChefProfileSearchBo searchBo);

    Long batchUpsert(@Param("chefProfiles") List<ChefProfile> chefProfiles);
}

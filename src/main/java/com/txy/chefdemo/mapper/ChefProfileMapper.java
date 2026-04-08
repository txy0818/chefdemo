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
    Long insert(@Param("chefProfile") ChefProfile chefProfile);

    int updateById(@Param("chefProfile") ChefProfile chefProfile);

    /**
     * 基于 INSERT ... ON DUPLICATE KEY UPDATE 的兜底写法。
     * 注意：MySQL 在唯一键冲突走 UPDATE 时，也可能推进 AUTO_INCREMENT，导致“吃号”。
     */
    Long upsert(@Param("chefProfile") ChefProfile chefProfile);

    ChefProfile queryByUserId(@Param("userId") Long userId);

    List<ChefProfile> queryChefListByCondition(@Param("searchBo") ChefProfileSearchBo searchBo);

    int queryChefListCnt(@Param("searchBo") ChefProfileSearchBo searchBo);

    /**
     * 批量 upsert 同样可能推进 AUTO_INCREMENT，仅保留兜底使用。
     */
    @Deprecated
    Long batchUpsert(@Param("chefProfiles") List<ChefProfile> chefProfiles);
}

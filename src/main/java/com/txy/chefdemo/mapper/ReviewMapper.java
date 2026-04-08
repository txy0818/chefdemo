package com.txy.chefdemo.mapper;

import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Mapper
public interface ReviewMapper {
    Long insert(@Param("review") Review review);

    int updateById(@Param("review") Review review);

    /**
     * 基于 INSERT ... ON DUPLICATE KEY UPDATE 的兜底写法。
     * 注意：MySQL 在唯一键冲突走 UPDATE 时，也可能推进 AUTO_INCREMENT，导致“吃号”。
     */
    Long upsert(@Param("review") Review review);

    List<Review> queryByCondition(@Param("searchBo") ReviewSearchBo reviewSearchBo);

    int queryCnt(@Param("searchBo") ReviewSearchBo reviewSearchBo);
}

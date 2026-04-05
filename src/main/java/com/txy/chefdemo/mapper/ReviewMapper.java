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
    Long upsert(@Param("review") Review review);

    List<Review> queryByCondition(@Param("searchBo") ReviewSearchBo reviewSearchBo);

    int queryCnt(@Param("searchBo") ReviewSearchBo reviewSearchBo);
}

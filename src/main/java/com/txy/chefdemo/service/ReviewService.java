package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ReviewService {
    Long upsert(@Param("review") Review review);

    List<Review> queryByCondition(ReviewSearchBo reviewSearchBo);

    int queryCnt(ReviewSearchBo reviewSearchBo);
}

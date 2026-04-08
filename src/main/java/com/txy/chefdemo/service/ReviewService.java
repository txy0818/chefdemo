package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ReviewService {
    Long insert(Review review);

    int updateById(Review review);

    /**
     * 仅保留兜底使用，业务代码优先走 insert / updateById。
     */
    Long upsert(Review review);

    List<Review> queryByCondition(ReviewSearchBo reviewSearchBo);

    int queryCnt(ReviewSearchBo reviewSearchBo);
}

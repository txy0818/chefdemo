package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.mapper.ReviewMapper;
import com.txy.chefdemo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Long insert(Review review) {
        return reviewMapper.insert(review);
    }

    @Override
    public int updateById(Review review) {
        return reviewMapper.updateById(review);
    }

    @Override
    public Long upsert(Review review) {
        return reviewMapper.upsert(review);
    }

    @Override
    public List<Review> queryByCondition(ReviewSearchBo reviewSearchBo) {
        return reviewMapper.queryByCondition(reviewSearchBo);
    }

    @Override
    public int queryCnt(ReviewSearchBo reviewSearchBo) {
        return reviewMapper.queryCnt(reviewSearchBo);
    }
}

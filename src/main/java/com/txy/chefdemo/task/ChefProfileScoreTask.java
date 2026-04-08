package com.txy.chefdemo.task;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.mapper.ChefProfileMapper;
import com.txy.chefdemo.mapper.ReviewMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author tianxinyu
 * @Create 2026-04-02
 */
@Slf4j
@Component
public class ChefProfileScoreTask {

    @Autowired
    private ChefProfileMapper chefProfileMapper;

    @Autowired
    private ReviewMapper reviewMapper;
    /**
     * 1. 每天统计最近一天新增且审核通过的评价数据。
     * 2. 按厨师维度汇总当天新增评分，并查询该厨师历史评价数量。
     * 3. 重新计算厨师综合评分后回写到厨师资料表。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateChefProfileScore() {
        log.info("开始执行定时任务：定时更新厨师评分");

        try {
            long now = System.currentTimeMillis();

            ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
            reviewSearchBo.setAuditStatus(AuditStatus.APPROVED.getCode());
            reviewSearchBo.setStartTime(now - 24 * 60 * 60 * 1000);
            reviewSearchBo.setEndTime(now);
            List<Review> reviews = reviewMapper.queryByCondition(reviewSearchBo);
            if (CollectionUtils.isEmpty(reviews)) {
                log.info("没有待处理的审核");
                return;
            }

            Map<Long, List<Review>> chefId2Reviews = reviews.stream().collect(Collectors.groupingBy(Review::getChefId));

            for (Map.Entry<Long, List<Review>> entry : chefId2Reviews.entrySet()) {
                Long chefId = entry.getKey();
                List<Review> list = entry.getValue();
                ReviewSearchBo searchBo = new ReviewSearchBo();
                searchBo.setAuditStatus(AuditStatus.APPROVED.getCode());
                searchBo.setChefId(chefId);
                int cnt = reviewMapper.queryCnt(searchBo);

                ChefProfileSearchBo chefProfileSearchBo = new ChefProfileSearchBo();
                chefProfileSearchBo.setUserId(chefId);
                List<ChefProfile> chefProfiles = chefProfileMapper.queryChefListByCondition(chefProfileSearchBo);
                if (CollectionUtils.isEmpty(chefProfiles)) {
                    log.info("厨师 {} 不存在", chefId);
                    continue;
                }

                ChefProfile chefProfile = chefProfiles.get(0);
                Long oldScore = chefProfile.getScore();
                long sum = list.stream().mapToLong(Review::getScore).sum();

                int totalCnt = cnt;
                int todayCnt = list.size();
                int oldCnt = totalCnt - todayCnt;

                long todaySum = sum;

                double newScore;
                if (oldCnt <= 0) {
                    newScore = todaySum * 1.0 / todayCnt;
                } else {
                    newScore = (oldScore * oldCnt + todaySum) / totalCnt;
                }

                chefProfile.setScore(Math.round(newScore));
                chefProfile.setUpdateTime(now);
                chefProfileMapper.updateById(chefProfile);

                log.info("厨师 {} 的评分已更新为 {}", chefId, newScore);
            }
        } catch (Exception e) {
            log.error("定时任务执行失败：定时更新厨师评分", e);
        }
    }
}

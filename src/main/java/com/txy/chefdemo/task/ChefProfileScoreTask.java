package com.txy.chefdemo.task;

import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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

    private static final long DEFAULT_SCORE = 500L;

    @Autowired
    private ChefProfileService chefProfileService;

    @Autowired
    private ReviewService reviewService;

    /**
     * 1. 每分钟扫描所有审核通过的厨师资料。
     * 2. 查询该厨师全部审核通过的评价，并按平均分重新计算综合评分。
     * 3. 没有审核通过评价时，评分回退为默认值 5.00 分。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void updateChefProfileScore() {
        log.info("开始执行定时任务：定时更新厨师评分");

        try {
            long now = System.currentTimeMillis();
            ChefProfileSearchBo chefProfileSearchBo = new ChefProfileSearchBo();
            chefProfileSearchBo.setAuditStatus(AuditStatus.APPROVED.getCode());
            List<ChefProfile> chefProfiles = chefProfileService.queryChefListByCondition(chefProfileSearchBo);
            if (CollectionUtils.isEmpty(chefProfiles)) {
                log.info("没有审核通过的厨师资料需要更新评分");
                return;
            }

            ReviewSearchBo reviewSearchBo = new ReviewSearchBo();
            reviewSearchBo.setAuditStatus(AuditStatus.APPROVED.getCode());
            List<Review> approvedReviews = reviewService.queryByCondition(reviewSearchBo);
            Map<Long, List<Review>> chefId2Reviews = approvedReviews.stream()
                    .filter(review -> ObjectUtils.isNotEmpty(review.getChefId()))
                    .collect(Collectors.groupingBy(Review::getChefId));

            for (ChefProfile chefProfile : chefProfiles) {
                Long chefId = chefProfile.getUserId();
                if (ObjectUtils.isEmpty(chefId)) {
                    continue;
                }
                List<Review> reviewList = chefId2Reviews.getOrDefault(chefId, List.of());
                long newScore = CollectionUtils.isEmpty(reviewList)
                        ? DEFAULT_SCORE
                        : Math.round(reviewList.stream().mapToLong(Review::getScore).average().orElse(DEFAULT_SCORE));
                chefProfile.setScore(newScore);
                chefProfile.setUpdateTime(now);
                chefProfileService.updateById(chefProfile);
                log.info("厨师 {} 的评分已更新为 {}", chefId, newScore);
            }
        } catch (Exception e) {
            log.error("定时任务执行失败：定时更新厨师评分", e);
        }
    }
}

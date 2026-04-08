package com.txy.chefdemo.task;

import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.mapper.ChefAvailableTimeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 厨师可预约时间段定时任务
 * 
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Slf4j
@Component
public class ChefAvailableTimeTask {

    @Autowired
    private ChefAvailableTimeMapper chefAvailableTimeMapper;

    /**
     * 1. 每分钟扫描仍处于可用类状态的时间段。
     * 2. 找出开始时间早于等于当前时间的过期时间段。
     * 3. 将这些时间段的状态批量更新为“已过期”。
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void updateExpiredTimeSlots() {
        log.info("开始执行定时任务：更新过期的时间段状态");
        
        try {
            long now = System.currentTimeMillis();
            // 查询所有未过期且开始时间已到的时间段
            ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
            searchBo.setStatus(AvailableTimeStatus.AVAILABLE.getCode());
            searchBo.setStartTimeLte(now);
            List<ChefAvailableTime> expiredTimeSlots = chefAvailableTimeMapper.queryByCondition(searchBo);
            
            if (CollectionUtils.isEmpty(expiredTimeSlots)) {
                log.info("没有需要更新的过期时间段");
                return;
            }
            
            log.info("找到 {} 个过期时间段，开始更新状态", expiredTimeSlots.size());
            
            // 批量更新状态为已过期
            int updatedCount = 0;
            for (ChefAvailableTime timeSlot : expiredTimeSlots) {
                timeSlot.setStatus(AvailableTimeStatus.EXPIRED.getCode());
                timeSlot.setUpdateTime(now);
                chefAvailableTimeMapper.updateById(timeSlot);
                updatedCount++;
            }
            log.info("定时任务执行完成，成功更新 {} 个时间段状态为已过期", updatedCount);
        } catch (Exception e) {
            log.error("定时任务执行失败：更新过期时间段状态", e);
        }
    }
}

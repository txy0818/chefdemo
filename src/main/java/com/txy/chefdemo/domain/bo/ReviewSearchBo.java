package com.txy.chefdemo.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSearchBo {
    private Long reviewId;
    private List<Long> reviewIds;
    private Long orderId;
    private List<Long> orderIds;
    private Long userId;
    private List<Long> userIds;
    private Long chefId;
    private List<Long> chefIds;
    private Integer auditStatus;
    private Long startTime;
    private Long endTime;
    private Long offset;
    private Long size;
}

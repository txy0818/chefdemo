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
public class ReportSearchBo {
    private Long reportId;
    private List<Long> reportIds;
    private Long orderId;
    private List<Long> orderIds;
    private Long userId;
    private List<Long> userIds;
    private Long targetUserId;
    private List<Long> targetUserIds;
    private Integer status;
    private List<Integer> statuses;
    private Long offset;
    private Long size;
}

package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    /** 评价ID */
    private Long id;
    /** 预约订单ID */
    private Long reservationOrderId;
    /** 用户name */
    private String userName;
    /** 厨师name */
    private String chefName;
    /** 评分 1-5分 */
    private String score;
    /** 评价内容 */
    private String content;
    private Integer auditStatus;
    /** 审核状态 */
    private String auditStatusDesc;
    /** 审核驳回原因 */
    private String auditReason;
    /** 评价时间 */
    private Long createTime;
    private String createTimeDesc;
}

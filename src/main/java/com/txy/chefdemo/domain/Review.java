package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    /** 评价ID */
    private Long id;
    /** 预约订单ID */
    private Long reservationOrderId;
    /** 用户ID */
    private Long userId;
    /** 厨师ID */
    private Long chefId;
    /** 评分 1-5分 */
    private Long score;
    /** 评价内容 */
    private String content;
    /** 审核状态 1-待审核 2-已通过 3-已驳回 */
    private Integer auditStatus;
    /** 审核驳回原因 */
    private String auditReason;
    /** 状态 1-正常 2-已删除 */
    private Integer status;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}

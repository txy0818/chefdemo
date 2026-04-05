package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    /** 举报ID */
    private Long id;
    /** 关联订单ID */
    private Long reservationOrderId;
    /** 举报人ID */
    private Long reporterId;
    /** 被举报用户ID */
    private Long targetUserId;
    /** 举报原因 */
    private String reason;
    /** 处理结果 */
    private String processResult;
    /** 处理管理员ID */
    private Long processedBy;
    /** 处理状态 0-待处理 1-举报属实 2-驳回 */
    private Integer status;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}

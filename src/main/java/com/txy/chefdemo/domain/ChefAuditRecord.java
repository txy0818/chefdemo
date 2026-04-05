package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefAuditRecord {
    /** 审核记录ID */
    private Long id;
    /** 厨师用户ID */
    private Long chefUserId;
    /** 审核结果 1-待审核 2-通过 3-拒绝 */
    private Integer auditStatus;
    /** 操作管理员ID */
    private Long operatorId;
    /** 审核拒绝原因*/
    private String rejectReason;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 审核完成时间(毫秒) */
    private Long auditTime;
}

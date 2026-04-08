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
    /** 评价 ID。 */
    private Long id;

    /** 预约订单 ID。 */
    private Long reservationOrderId;

    /** 用户名。 */
    private String userName;

    /** 厨师名。 */
    private String chefName;

    /** 评分描述。 */
    private String score;

    /** 评价内容。 */
    private String content;

    /** 审核状态编码。 */
    private Integer auditStatus;

    /** 审核状态描述。 */
    private String auditStatusDesc;

    /** 审核驳回原因。 */
    private String auditReason;

    /** 评价时间，毫秒时间戳。 */
    private Long createTime;

    /** 评价时间描述。 */
    private String createTimeDesc;
}

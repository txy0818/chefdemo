package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    /** 关联订单 ID。 */
    private Long reservationOrderId;

    /** 举报人用户名。 */
    private String reporterName;

    /** 被举报用户名。 */
    private String targetUserName;

    /** 举报原因。 */
    private String reason;

    /** 处理结果。 */
    private String processResult;

    /** 处理状态编码。 */
    private Integer status;

    /** 处理状态描述。 */
    private String statusDesc;
}

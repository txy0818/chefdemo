package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRecord {
    /** 流水ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 关联预约订单ID */
    private Long reservationOrderId;
    /** 变动金额(分) */
    private Long amount;
    /** 类型 1-充值 2-支付 3-退款 */
    private Integer type;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}

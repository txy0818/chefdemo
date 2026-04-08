package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRecordDTO {
    /** 钱包流水 ID。 */
    private Long id;

    /** 关联订单 ID。 */
    private Long reservationOrderId;

    /** 变动金额，单位为分。 */
    private Long amount;

    /** 变动金额描述，金额以元为单位。 */
    private String amountDesc;

    /** 流水类型编码。 */
    private Integer type;

    /** 流水类型描述。 */
    private String typeDesc;

    /** 创建时间，毫秒时间戳。 */
    private Long createTime;

    /** 创建时间描述。 */
    private String createTimeDesc;
}

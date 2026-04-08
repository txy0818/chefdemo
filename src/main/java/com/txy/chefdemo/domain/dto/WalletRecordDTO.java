package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletRecordDTO {
    private Long id;
    private Long reservationOrderId;
    private Long amount;
    // 元
    private String amountDesc;
    private Integer type;
    private String typeDesc;
    private Long createTime;
    private String createTimeDesc;
}

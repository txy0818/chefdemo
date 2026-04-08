package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletDTO {
    private Long walletId;
    private Long userId;
    /** 当前余额(分) */
    private Long balance;
    // 元
    private String balanceDesc;
}

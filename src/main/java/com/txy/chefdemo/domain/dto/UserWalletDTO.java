package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletDTO {
    /** 钱包 ID。 */
    private Long walletId;

    /** 用户 ID。 */
    private Long userId;

    /** 当前余额，单位为分。 */
    private Long balance;

    /** 当前余额描述，金额以元为单位。 */
    private String balanceDesc;
}

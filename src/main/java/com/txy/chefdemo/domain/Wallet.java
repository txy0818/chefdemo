package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    /** 钱包ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 当前余额(分) */
    private Long balance;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}
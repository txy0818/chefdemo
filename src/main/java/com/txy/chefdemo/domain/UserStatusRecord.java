package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusRecord {
    /** 状态记录ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 变更前状态 */
    private Integer beforeStatus;
    /** 变更后状态 */
    private Integer afterStatus;
    /** 变更原因 */
    private String reason;
    /** 操作管理员ID */
    private Long operatorId;
    /** 创建时间(毫秒) */
    private Long createTime;
}

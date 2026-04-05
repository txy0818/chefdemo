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
public class UserDTO {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 角色 */
    private String role;
    /** 头像URL */
    private String avatar;
    /** 手机号 */
    private String phone;
    /** 状态 */
    private String status;
    /** 最后登录时间 xxxx-xx-xx xx:xx:xx */
    private String lastLoginTime;
}

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
    /** 用户 ID。 */
    private Long id;

    /** 用户名。 */
    private String username;

    /** 用户角色编码。 */
    private Integer role;

    /** 用户角色描述。 */
    private String roleDesc;

    /** 头像 URL。 */
    private String avatar;

    /** 手机号。 */
    private String phone;

    /** 用户状态编码。 */
    private Integer status;

    /** 用户状态描述。 */
    private String statusDesc;

    /** 最后登录时间，毫秒时间戳。 */
    private Long lastLoginTime;

    /** 最后登录时间描述，格式为 yyyy-MM-dd HH:mm:ss。 */
    private String lastLoginTimeDesc;
}

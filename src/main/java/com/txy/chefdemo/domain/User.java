package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 密码(加密) */
    private String password;
    /** 角色 1-管理员 2-厨师 3-普通用户 */
    private Integer role;
    /** 头像URL */
    private String avatar;
    /** 手机号 */
    private String phone;
    /** 状态 1-正常 2-冻结 */
    private Integer status;
    /** 最后登录时间(毫秒) */
    private Long lastLoginTime;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}

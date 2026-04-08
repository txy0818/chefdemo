package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReq {
    /** 用户名。 */
    private String username;

    /** 手机号。 */
    private String phone;

    /** 明文密码。 */
    private String password;

    /** 用户角色编码。 */
    private Integer userRole;
}

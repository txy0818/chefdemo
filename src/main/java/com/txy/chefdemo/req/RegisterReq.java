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
    private String username;
    private String phone;
    private String password;
    private Integer userRole; // 1-管理员, 2-厨师, 3-普通用户
}

package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogInReq {
    private String username;
    private String password;
    private Integer role;
}

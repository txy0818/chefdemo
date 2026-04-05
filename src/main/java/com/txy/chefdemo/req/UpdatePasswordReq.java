package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-04-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordReq {
    private String oldPassword = "";
    private String newPassword = "";
}

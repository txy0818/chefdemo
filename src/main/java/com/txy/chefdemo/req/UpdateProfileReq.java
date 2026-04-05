package com.txy.chefdemo.req;

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
public class UpdateProfileReq {
    private Long id = 0L;
    private String avatar = "";
    private String phone = "";
}

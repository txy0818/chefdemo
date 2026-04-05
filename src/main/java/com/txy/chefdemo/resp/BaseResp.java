package com.txy.chefdemo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-01-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResp {
    private int code;
    private String desc;
}

package com.txy.chefdemo.resp;

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
public class LogInResp {
    private BaseResp baseResp;
    private String token = "";
    private String userName = "";
    private String userId = "";


    public LogInResp(BaseResp baseResp) {
        this.baseResp = baseResp;
    }
}

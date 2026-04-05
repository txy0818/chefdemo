package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author tianxinyu
 * @Create 2026-03-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryChefReq {
    private Long userId = 0L;
    private String username = "";
    private Integer auditStatus = 0;
    private Long page = 0L;
    private Long size = 10L;
}

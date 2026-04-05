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
public class ListAvailableTimesReq {
    private Long page = 0L;
    private Long size = 0L;
}

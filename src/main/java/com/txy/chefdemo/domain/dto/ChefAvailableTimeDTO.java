package com.txy.chefdemo.domain.dto;

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
public class ChefAvailableTimeDTO {
    private Long id;
    /** 可预约开始时间(毫秒) */
    private Long startTime;
    /** 可预约结束时间(毫秒) */
    private Long endTime;
    /** 状态 1-可预约 2-已约满 3-已过期 4-已删除 */
    private Integer status;
    private String statusDesc;
}

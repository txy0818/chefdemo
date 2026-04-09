package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private String startTimeDesc;
    /** 可预约结束时间(毫秒) */
    private Long endTime;
    private String endTimeDesc;
    /** 状态 1-可预约 2-已约满 3-已过期 4-已删除 */
    private Integer status;
    private String statusDesc;
    /** 该时间段内已预约的子时间段描述。 */
    private List<String> occupiedTimeDescList;
}

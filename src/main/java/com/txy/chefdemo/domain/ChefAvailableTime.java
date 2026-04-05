package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefAvailableTime {
    /** 时间段ID */
    private Long id;
    /** 厨师ID */
    private Long chefId;
    /** 可预约开始时间(毫秒) */
    private Long startTime;
    /** 可预约结束时间(毫秒) */
    private Long endTime;
    /** 状态 1-可预约 2-已约满 3-已过期 4-已删除 */
    private Integer status;
    /** 创建时间(毫秒) */
    private Long createTime;
    /** 更新时间(毫秒) */
    private Long updateTime;
}

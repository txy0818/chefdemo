package com.txy.chefdemo.domain.bo;

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
public class ChefAvailableTimeSearchBo {
    private Long id;
    private List<Long> idList;
    private Long chefId;
    private List<Long> chefIdList;
    private Long startTime;
    private Long endTime;
    private Integer status;
    private List<Integer> statusList;
    private Long offset;
    private Long size;
}

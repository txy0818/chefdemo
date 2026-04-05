package com.txy.chefdemo.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChefProfileSearchBo {
    private Long userId;
    private List<Long> userIdList;
    private String userName;
    private Integer auditStatus;
    private Long time;
    private String serviceArea;
    private Long minScore;
    private Long maxScore;
    private Long minPrice;
    private Long maxPrice;
    private String sortType;
    private Long offset;
    private Long size;
}

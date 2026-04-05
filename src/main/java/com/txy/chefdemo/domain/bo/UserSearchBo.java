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
public class UserSearchBo {
    private Long userId;
    private List<Long> userIdList;
    private String userName;
    private Integer status;
    private Integer role;
    private Long startTime;
    private Long endTime;
    private Long offset;
    private Long size;

}

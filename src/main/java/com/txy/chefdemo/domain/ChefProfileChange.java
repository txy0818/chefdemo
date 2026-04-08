package com.txy.chefdemo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefProfileChange {
    private Long id;
    private Long userId;
    private String avatar;
    private String displayName;
    private String realName;
    private String idCardImgs;
    private String healthCertImgs;
    private String chefCertImgs;
    private String cuisineType;
    private String serviceArea;
    private String serviceDesc;
    private Long price;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer age;
    private Integer gender;
    private Integer workYears;
    private String phone;
    private Integer auditStatus;
    private String rejectReason;
    private Long createTime;
    private Long updateTime;
    private Long auditTime;
}

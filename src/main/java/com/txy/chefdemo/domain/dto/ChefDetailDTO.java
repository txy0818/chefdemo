package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefDetailDTO {
    private Long chefUserId;
    private String avatar;
    private String displayName;
    private Integer age;
    private String gender;
    private Integer workYears;
    private List<String> cuisineType;
    private String serviceArea;
    private Long price;
    private Integer minPeople;
    private Integer maxPeople;
    private String serviceDesc;
    private List<String> healthCertImgs;
    private List<String> chefCertImgs;
    private Double score;
}

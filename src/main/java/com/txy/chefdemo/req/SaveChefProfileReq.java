package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveChefProfileReq {
    private String realName;
    private List<String> idCardImgs;
    private List<String> healthCertImgs;
    private List<String> chefCertImgs;
    // [1, 2, 3]
    private List<Integer> cuisineType;
    private String serviceArea;
    private String serviceDesc;
    // 元
    private String price;
    private Integer minPeople;
    private Integer maxPeople;
    private Integer age;
    private Integer gender;
    private Integer workYears;
}

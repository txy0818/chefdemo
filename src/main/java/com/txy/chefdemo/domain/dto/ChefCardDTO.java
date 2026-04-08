package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChefCardDTO {
    private Long chefUserId;
    private String avatar;
    private String displayName;
    private List<String> cuisineTypeDesc;
    private List<Integer> cuisineType;
    private String serviceArea;
    private Long price;
    private String priceDesc;
    private Double score;
}

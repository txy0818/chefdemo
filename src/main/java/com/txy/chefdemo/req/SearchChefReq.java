package com.txy.chefdemo.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchChefReq {
    private String keyword = "";
    private List<Integer> cuisineTypeList = new ArrayList<>();
    private String serviceArea = "";
    private Long minPrice = 0L;
    private Long maxPrice = 0L;
    private Double minScore = 0.0d;
    private Double maxScore = 0.0d;
    private String sortType = "";
    private Long page = 0L;
    private Long size = 0L;
}

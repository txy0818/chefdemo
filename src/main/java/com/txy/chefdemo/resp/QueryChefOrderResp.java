package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.dto.OrderViewDTO;
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
public class QueryChefOrderResp {
    private BaseResp baseResp;
    private List<OrderViewDTO> data;
    private int total;
}

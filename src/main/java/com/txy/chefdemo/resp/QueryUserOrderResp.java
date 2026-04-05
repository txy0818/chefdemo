package com.txy.chefdemo.resp;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-04-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryUserOrderResp {
    private BaseResp baseResp;
    private List<OrderViewDTO> data;
    private int total;
}

package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.dto.OrderViewDTO;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public interface ReservationOrderService {
    Long insert(ReservationOrder order);

    int updateById(ReservationOrder order);

    List<ReservationOrder> queryByCondition(ReservationOrderSearchBo searchBo);

    int queryCnt(ReservationOrderSearchBo searchBo);

    OrderViewDTO buildOrderView(ReservationOrder order);
}

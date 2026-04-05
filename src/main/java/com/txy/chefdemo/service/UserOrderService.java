package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.req.CancelOrderReq;
import com.txy.chefdemo.req.CreateOrderReq;
import com.txy.chefdemo.req.PayOrderReq;

public interface UserOrderService {

    OrderViewDTO createOrder(Long currentUserId, CreateOrderReq req);

    void payOrder(Long currentUserId, PayOrderReq req);

    void cancelOrder(Long currentUserId, CancelOrderReq req);
}

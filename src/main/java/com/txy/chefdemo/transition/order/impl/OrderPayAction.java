package com.txy.chefdemo.transition.order.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.Wallet;
import com.txy.chefdemo.domain.WalletRecord;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.constant.PaymentType;
import com.txy.chefdemo.domain.constant.WalletRecordType;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.WalletRecordService;
import com.txy.chefdemo.service.WalletService;
import com.txy.chefdemo.transition.order.OrderAction;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.support.OrderTransitionSupport;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderPayAction implements OrderAction {

    @Autowired
    private OrderTransitionSupport support;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRecordService walletRecordService;

    /**
     * 1. 查询订单并校验当前操作人是否为下单用户本人。
     * 2. 根据支付方式执行支付，目前余额支付会扣减钱包并新增支付流水。
     * 3. 更新订单支付状态、支付时间，并把订单状态流转为“待接单”。
     * 4. 向用户和厨师发送支付成功后的订单通知。
     * 5. 回写最新订单状态。
     */
    @Override
    public ReservationOrder execute(OrderContext context) {
        ReservationOrder order = support.queryOrder(context.getOrderId());
        Preconditions.checkArgument(Objects.equals(order.getUserId(), context.getOperatorUserId()), BaseRespConstant.FORBIDDEN.getDesc());

        long now = System.currentTimeMillis();
        Integer payType = ObjectUtils.defaultIfNull(context.getPayType(), PaymentType.WALLET.getCode());
        if (Objects.equals(payType, PaymentType.WALLET.getCode())) {
            Wallet wallet = walletService.queryByUserId(context.getOperatorUserId());
            Preconditions.checkArgument(ObjectUtils.isNotEmpty(wallet), "钱包不存在");
            Preconditions.checkArgument(wallet.getBalance() >= order.getTotalAmount(), BaseRespConstant.BALANCE_NOT_ENOUGH.getDesc());
            wallet.setBalance(wallet.getBalance() - order.getTotalAmount());
            wallet.setUpdateTime(now);
            walletService.updateById(wallet);
            walletRecordService.insert(new WalletRecord(null, order.getUserId(), order.getId(), order.getTotalAmount(),
                    WalletRecordType.PAYMENT.getCode(), now, now));
        }

        order.setPayStatus(PayStatus.PAID.getCode());
        order.setPayTime(now);
        order.setStatus(OrderStatus.PENDING_ACCEPT.getCode());
        ReservationOrder updatedOrder = support.updateOrder(order);
        support.createBothSideNotification(
                updatedOrder,
                "订单状态更新",
                "您的订单已支付成功，当前状态为“" + OrderStatus.PENDING_ACCEPT.getDesc() + "”。订单ID：" + updatedOrder.getId(),
                "订单状态更新",
                "您有一笔新订单待处理，当前状态为“" + OrderStatus.PENDING_ACCEPT.getDesc() + "”。订单ID：" + updatedOrder.getId()
        );
        return updatedOrder;
    }
}

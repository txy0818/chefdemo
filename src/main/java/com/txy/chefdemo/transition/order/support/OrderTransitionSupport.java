package com.txy.chefdemo.transition.order.support;

import com.txy.chefdemo.domain.*;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.domain.constant.NotificationReadStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.constant.WalletRecordType;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.service.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Component
public class OrderTransitionSupport {

    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private ChefAvailableTimeService chefAvailableTimeService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRecordService walletRecordService;
    @Autowired
    private NotificationRecordService notificationRecordService;
    @Autowired
    private NotificationService notificationService;

    public ReservationOrder queryOrder(Long orderId) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(orderId);
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException("订单不存在");
        }
        return orders.get(0);
    }

    public ReservationOrder updateOrder(ReservationOrder order) {
        order.setUpdateTime(System.currentTimeMillis());
        reservationOrderService.updateById(order);
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(order.getId());
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        return CollectionUtils.isEmpty(orders) ? null : orders.get(0);
    }

    public void releaseTime(Long timeId) {
        if (Objects.isNull(timeId)) {
            return;
        }
        ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
        searchBo.setId(timeId);
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(times)) {
            return;
        }
        ChefAvailableTime time = times.get(0);
        time.setStatus(AvailableTimeStatus.AVAILABLE.getCode());
        time.setUpdateTime(System.currentTimeMillis());
        chefAvailableTimeService.updateById(time);
    }

    public void refundIfPaid(ReservationOrder order, String notificationContent) {
        refundIfPaid(order);
        createNotification(order.getUserId(), "订单退款通知", notificationContent);
    }

    public void refundIfPaid(ReservationOrder order, String notificationContent, String source) {
        refundIfPaid(order);
        createNotification(order.getUserId(), "订单退款通知", appendSource(notificationContent, source));
    }

    public void refundIfPaid(ReservationOrder order) {
        if (!Objects.equals(PayStatus.PAID.getCode(), order.getPayStatus())) {
            return;
        }
        long now = System.currentTimeMillis();
        Wallet wallet = walletService.queryByUserId(order.getUserId());
        if (ObjectUtils.isNotEmpty(wallet)) {
            wallet.setBalance(wallet.getBalance() + order.getTotalAmount());
            wallet.setUpdateTime(now);
            walletService.updateById(wallet);
        }
        walletRecordService.insert(new WalletRecord(null, order.getUserId(), order.getId(), order.getTotalAmount(),
                WalletRecordType.REFUND.getCode(), now, now));
    }

    public void createNotification(Long userId, String title, String content) {
        if (ObjectUtils.isEmpty(userId)) {
            return;
        }
        long now = System.currentTimeMillis();
        NotificationRecord notificationRecord = new NotificationRecord(null, userId, title, content,
                NotificationReadStatus.UNREAD.getCode(), now, now);
        notificationRecordService.insert(notificationRecord);
        notificationService.notifyUser(notificationRecord);
    }

    public void createNotification(Long userId, String title, String content, String source) {
        createNotification(userId, title, appendSource(content, source));
    }

    public void createBothSideNotification(ReservationOrder order,
                                           String userTitle,
                                           String userContent,
                                           String chefTitle,
                                           String chefContent) {
        createNotification(order.getUserId(), userTitle, userContent);
        createNotification(order.getChefId(), chefTitle, chefContent);
    }

    public void createBothSideNotification(ReservationOrder order,
                                           String userTitle,
                                           String userContent,
                                           String chefTitle,
                                           String chefContent,
                                           String source) {
        createNotification(order.getUserId(), userTitle, userContent, source);
        createNotification(order.getChefId(), chefTitle, chefContent, source);
    }

    private String appendSource(String content, String source) {
        if (Objects.isNull(source) || source.isBlank()) {
            return content;
        }
        return content + " [来源:" + source + "]";
    }
}

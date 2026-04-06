package com.txy.chefdemo.event;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.mapper.ReservationOrderMapper;
import com.txy.chefdemo.mapper.UserMapper;
import com.txy.chefdemo.service.FrozenChefCleanupService;
import com.txy.chefdemo.service.OrderFlowService;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class UserStatusChangeListener {

    @Resource
    private ReservationOrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Autowired
    private OrderFlowService orderFlowService;
    @Autowired
    private FrozenChefCleanupService frozenChefCleanupService;

    private static final String FROZEN_STR = "厨师账号被冻结，订单已"
            + OrderStatus.CANCELLED.getDesc() + "/" + PayStatus.REFUNDED.getDesc();
    private static final String SOURCE = "user-status-listener";

    @EventListener
    @Async("orderCancelExecutor")
    public void handle(UserStatusChangeEvent event) {
        // 只处理冻结
        if (!Objects.equals(event.getStatus(), UserStatus.FROZEN.getCode())) {
            return;
        }

        Long userId = event.getUserId();
        User user = userMapper.queryById(userId);
        if (user == null) {
            log.warn("[{}] userId={} 状态变更事件未找到用户，跳过处理", SOURCE, userId);
            return;
        }
        String cancelReason = user.getUsername() + " " + FROZEN_STR;
        long now = System.currentTimeMillis();
        log.info("[{}] userId={} 开始处理冻结账号联动逻辑", SOURCE, userId);

        // 主处理链路：
        // 冻结后立即做一轮资料/评论/举报清理，避免无订单时只能等待定时任务兜底。
        frozenChefCleanupService.cleanupFrozenChef(userId, event.getOperatorId(), now);
        log.info("[{}] userId={} 已完成资料/评论/举报清理", SOURCE, userId);

        // 1. 查询未完成已支付订单（待接单、已接单）
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setChefId(userId);
        searchBo.setStatuses(Arrays.asList(OrderStatus.PENDING_ACCEPT.getCode(), OrderStatus.ACCEPTED.getCode()));
        List<ReservationOrder> orders = orderMapper.queryByCondition(searchBo);
        
        if (!CollectionUtils.isEmpty(orders)) {
            for (ReservationOrder order : orders) {
                try {
                    OrderContext context = new OrderContext(order.getId(), event.getOperatorId(), null, cancelReason);
                    context.setSource(SOURCE);
                    orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()),
                            OrderStateEvent.CHEF_FROZEN_CANCEL,
                            context);
                    log.info("[{}] userId={}, orderId={} 已支付未完成订单关闭成功", SOURCE, userId, order.getId());
                } catch (Exception e) {
                    log.error("[{}] userId={}, orderId={} 已支付未完成订单关闭失败", SOURCE, userId, order.getId(), e);
                }
            }
        }

        // 2. 查询未完成未支付订单（待支付）
        ReservationOrderSearchBo pendingSearchBo = new ReservationOrderSearchBo();
        pendingSearchBo.setChefId(userId);
        pendingSearchBo.setStatuses(Arrays.asList(OrderStatus.PENDING_PAYMENT.getCode()));
        List<ReservationOrder> pendingOrders = orderMapper.queryByCondition(pendingSearchBo);
        
        if (!CollectionUtils.isEmpty(pendingOrders)) {
            for (ReservationOrder order : pendingOrders) {
                try {
                    OrderContext context = new OrderContext(order.getId(), event.getOperatorId(), null, cancelReason);
                    context.setSource(SOURCE);
                    orderFlowService.trigger(OrderStatus.PENDING_PAYMENT,
                            OrderStateEvent.CHEF_FROZEN_CANCEL,
                            context);
                    log.info("[{}] userId={}, orderId={} 未支付订单关闭成功", SOURCE, userId, order.getId());
                } catch (Exception e) {
                    log.error("[{}] userId={}, orderId={} 未支付订单关闭失败", SOURCE, userId, order.getId(), e);
                }
            }
        }
        log.info("[{}] userId={} 冻结账号联动主处理链路完成", SOURCE, userId);
    }
}

package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.CancelOrderReq;
import com.txy.chefdemo.req.CreateOrderReq;
import com.txy.chefdemo.req.PayOrderReq;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.ChefAvailableTimeService;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.OrderFlowService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserOrderService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class UserOrderServiceImpl implements UserOrderService {

    private static final String SRC = "user";
    private static final String CREATE_ORDER_LOCK_PREFIX = "order:create:";
    private static final long CREATE_ORDER_LOCK_WAIT_SECONDS = 3L;
    private static final long CREATE_ORDER_LOCK_LEASE_SECONDS = 10L;
    private static final long MIN_TIME_RANGE_MILLIS = 10 * 60 * 1000L;

    @Autowired
    private UserService userService;
    @Autowired
    private ChefProfileService chefProfileService;
    @Autowired
    private ChefAvailableTimeService chefAvailableTimeService;
    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private OrderFlowService orderFlowService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public OrderViewDTO createOrder(Long currentUserId, CreateOrderReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getChefUserId()), "厨师不能为空");
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getChefAvailableTimeId()), "时间段不能为空");
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getStartTime()) && ObjectUtils.isNotEmpty(req.getEndTime()), "预约时间不能为空");
        Preconditions.checkArgument(req.getStartTime() < req.getEndTime(), "开始时间必须早于结束时间");
        Preconditions.checkArgument(req.getEndTime() - req.getStartTime() > MIN_TIME_RANGE_MILLIS, "预约时间间隔必须大于10分钟");
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getPeopleCount()) && req.getPeopleCount() > 0, "人数非法");
        Preconditions.checkArgument(req.getStartTime() > System.currentTimeMillis(), "时间段已过期");
        RLock lock = redissonClient.getLock(CREATE_ORDER_LOCK_PREFIX + req.getChefAvailableTimeId());
        boolean locked = false;
        try {
            locked = lock.tryLock(CREATE_ORDER_LOCK_WAIT_SECONDS, CREATE_ORDER_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("当前时间段下单人数较多，请稍后重试");
            }
            return transactionTemplate.execute(status -> doCreateOrder(currentUserId, req));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("当前下单人数较多，请稍后重试");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private OrderViewDTO doCreateOrder(Long currentUserId, CreateOrderReq req) {
        User user = userService.queryById(currentUserId);
        if (ObjectUtils.isEmpty(user)) {
            throw new BusinessException("用户不存在");
        }

        ChefProfile profile = chefProfileService.queryByUserId(req.getChefUserId());
        if (ObjectUtils.isEmpty(profile)) {
            throw new BusinessException("厨师不存在");
        }
        if (!Objects.equals(profile.getAuditStatus(), AuditStatus.APPROVED.getCode())) {
            throw new BusinessException(BaseRespConstant.AUDIT_NOT_PASS.getDesc());
        }

        long now = System.currentTimeMillis();
        ChefAvailableTimeSearchBo timeSearchBo = new ChefAvailableTimeSearchBo();
        timeSearchBo.setId(req.getChefAvailableTimeId());
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(timeSearchBo);
        if (CollectionUtils.isEmpty(times)) {
            throw new BusinessException("时间段不存在");
        }
        ChefAvailableTime time = times.get(0);
        if (!Objects.equals(time.getChefId(), req.getChefUserId())
                || !Objects.equals(time.getStatus(), AvailableTimeStatus.AVAILABLE.getCode())
                || time.getEndTime() < now) {
            throw new BusinessException(BaseRespConstant.TIME_NOT_AVAILABLE.getDesc());
        }
        if (req.getStartTime() < time.getStartTime() || req.getEndTime() > time.getEndTime()) {
            throw new BusinessException("预约时间不在时间段内");
        }

        ReservationOrderSearchBo orderSearchBo = new ReservationOrderSearchBo();
        orderSearchBo.setChefAvailableTimeId(req.getChefAvailableTimeId());
        orderSearchBo.setStatuses(OrderStatus.getValidCodes());
        List<ReservationOrder> existingOrders = reservationOrderService.queryByCondition(orderSearchBo);
        long minGap = 30 * 60 * 1000L;
        for (ReservationOrder existingOrder : existingOrders) {
            if (!(req.getEndTime() + minGap <= existingOrder.getStartTime()
                    || req.getStartTime() >= existingOrder.getEndTime() + minGap)) {
                throw new BusinessException(BaseRespConstant.TIME_NOT_AVAILABLE.getDesc());
            }
        }

        long totalTime = req.getEndTime() - req.getStartTime();
        long avgPrice = ObjectUtils.defaultIfNull(profile.getPrice(), 0L);
        long totalAmount = Math.max(avgPrice, (long) Math.ceil(avgPrice * (totalTime / 3600000D)));

        ReservationOrder order = new ReservationOrder();
        order.setUserId(currentUserId);
        order.setChefId(req.getChefUserId());
        order.setChefAvailableTimeId(req.getChefAvailableTimeId());
        order.setStartTime(req.getStartTime());
        order.setEndTime(req.getEndTime());
        order.setTotalTime(totalTime);
        order.setTotalAmount(totalAmount);
        order.setPeopleCount(req.getPeopleCount());
        order.setSpecialRequirements(req.getSpecialRequirements());
        order.setContactName(req.getContactName());
        order.setContactPhone(req.getContactPhone());
        order.setContactAddress(req.getContactAddress());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setPayStatus(PayStatus.UNPAID.getCode());
        order.setPayDeadlineTime(now + 5 * 60 * 1000L);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        reservationOrderService.insert(order);

        RDelayedQueue<Long> delayedQueue = redissonClient.getDelayedQueue(
                redissonClient.getBlockingQueue("order:cancel:queue"));
        delayedQueue.offer(order.getId(), 5, TimeUnit.MINUTES);

        return reservationOrderService.buildOrderView(order);
    }

    @Override
    @Transactional
    public void payOrder(Long currentUserId, PayOrderReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        ReservationOrder order = requireOwnedOrder(currentUserId, req.getOrderId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_PAYMENT.getCode())) {
            throw new BusinessException(BaseRespConstant.STATUS_ERROR.getDesc());
        }
        OrderContext orderContext = new OrderContext(order.getId(), currentUserId, req.getPayType(), null);
        orderContext.setSource(SRC);
        orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()), OrderStateEvent.PAY, orderContext);
    }

    @Override
    @Transactional
    public void cancelOrder(Long currentUserId, CancelOrderReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        Preconditions.checkArgument(org.apache.commons.lang3.StringUtils.isNotBlank(req.getCancelReason()), "取消原因不能为空");
        String cancelReason = req.getCancelReason().trim();
        ReservationOrder order = requireOwnedOrder(currentUserId, req.getOrderId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_PAYMENT.getCode())
                && !Objects.equals(order.getStatus(), OrderStatus.PENDING_ACCEPT.getCode())) {
            throw new BusinessException(BaseRespConstant.STATUS_ERROR.getDesc());
        }
        OrderContext orderContext = new OrderContext(order.getId(), currentUserId, null, cancelReason);
        orderContext.setSource(SRC);
        orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()), OrderStateEvent.USER_CANCEL, orderContext);
    }

    private ReservationOrder requireOwnedOrder(Long currentUserId, Long orderId) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(orderId);
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException("订单不存在");
        }
        ReservationOrder order = orders.get(0);
        if (!Objects.equals(order.getUserId(), currentUserId)) {
            throw new BusinessException(BaseRespConstant.FORBIDDEN.getDesc());
        }
        return order;
    }
}

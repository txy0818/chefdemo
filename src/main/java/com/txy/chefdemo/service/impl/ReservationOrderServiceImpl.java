package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.mapper.ReservationOrderMapper;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
@Service
public class ReservationOrderServiceImpl implements ReservationOrderService {
    @Autowired
    private ReservationOrderMapper reservationOrderMapper;
    @Autowired
    private UserService userService;

    @Override
    public Long updateById(ReservationOrder order) {
        return reservationOrderMapper.updateById(order);
    }

    @Override
    public Long insert(ReservationOrder order) {
        return reservationOrderMapper.insert(order);
    }

    @Override
    public List<ReservationOrder> queryByCondition(ReservationOrderSearchBo searchBo) {
        return reservationOrderMapper.queryByCondition(searchBo);
    }

    @Override
    public int queryCnt(ReservationOrderSearchBo searchBo) {
        return reservationOrderMapper.queryCnt(searchBo);
    }

    @Override
    public OrderViewDTO buildOrderView(ReservationOrder order) {
        User user = userService.queryById(order.getUserId());
        User chef = userService.queryById(order.getChefId());

        OrderViewDTO dto = new OrderViewDTO();
        dto.setId(ObjectUtils.defaultIfNull(order.getId(), 0L));
        dto.setUserName(ObjectUtils.isNotEmpty(user) && StringUtils.isNotBlank(user.getUsername()) ? user.getUsername() : "-");
        dto.setChefName(ObjectUtils.isNotEmpty(chef) && StringUtils.isNotBlank(chef.getUsername()) ? chef.getUsername() : "-");
        dto.setStartTime(ObjectUtils.defaultIfNull(order.getStartTime(), 0L));
        dto.setEndTime(ObjectUtils.defaultIfNull(order.getEndTime(), 0L));
        dto.setTotalAmount(ObjectUtils.defaultIfNull(order.getTotalAmount(), 0L));
        dto.setPeopleCount(ObjectUtils.defaultIfNull(order.getPeopleCount(), 0));
        dto.setSpecialRequirements(StringUtils.isNotBlank(order.getSpecialRequirements()) ? order.getSpecialRequirements() : "-");
        dto.setContactName(StringUtils.isNotBlank(order.getContactName()) ? order.getContactName() : "-");
        dto.setContactPhone(StringUtils.isNotBlank(order.getContactPhone()) ? order.getContactPhone() : "-");
        dto.setContactAddress(StringUtils.isNotBlank(order.getContactAddress()) ? order.getContactAddress() : "-");
        dto.setStatus(ObjectUtils.defaultIfNull(order.getStatus(), 0));
        dto.setStatusDesc(ObjectUtils.isNotEmpty(order.getStatus()) ? OrderStatus.fromCode(order.getStatus()).getDesc() : "-");
        dto.setPayStatus(ObjectUtils.defaultIfNull(order.getPayStatus(), 0));
        dto.setPayStatusDesc(ObjectUtils.isNotEmpty(order.getPayStatus()) ? PayStatus.fromCode(order.getPayStatus()).getDesc() : "-");
        dto.setCancelReason(StringUtils.isNotBlank(order.getCancelReason()) ? order.getCancelReason() : "-");
        dto.setPayDeadlineTime(ObjectUtils.defaultIfNull(order.getPayDeadlineTime(), 0L));
        return dto;
    }

}

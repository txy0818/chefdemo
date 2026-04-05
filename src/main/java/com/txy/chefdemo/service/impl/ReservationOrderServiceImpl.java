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
        dto.setId(order.getId() != null ? order.getId() : 0L);
        dto.setUserName(user != null && StringUtils.isNotBlank(user.getUsername()) ? user.getUsername() : "-");
        dto.setChefName(chef != null && StringUtils.isNotBlank(chef.getUsername()) ? chef.getUsername() : "-");
        dto.setStartTime(order.getStartTime() != null ? order.getStartTime() : 0L);
        dto.setEndTime(order.getEndTime() != null ? order.getEndTime() : 0L);
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0L);
        dto.setPeopleCount(order.getPeopleCount() != null ? order.getPeopleCount() : 0);
        dto.setSpecialRequirements(StringUtils.isNotBlank(order.getSpecialRequirements()) ? order.getSpecialRequirements() : "-");
        dto.setContactName(StringUtils.isNotBlank(order.getContactName()) ? order.getContactName() : "-");
        dto.setContactPhone(StringUtils.isNotBlank(order.getContactPhone()) ? order.getContactPhone() : "-");
        dto.setContactAddress(StringUtils.isNotBlank(order.getContactAddress()) ? order.getContactAddress() : "-");
        dto.setStatus(order.getStatus() != null ? order.getStatus() : 0);
        dto.setStatusDesc(order.getStatus() != null ? OrderStatus.fromCode(order.getStatus()).getDesc() : "-");
        dto.setPayStatus(order.getPayStatus() != null ? order.getPayStatus() : 0);
        dto.setPayStatusDesc(order.getPayStatus() != null ? PayStatus.fromCode(order.getPayStatus()).getDesc() : "-");
        dto.setCancelReason(StringUtils.isNotBlank(order.getCancelReason()) ? order.getCancelReason() : "-");
        dto.setPayDeadlineTime(order.getPayDeadlineTime() != null ? order.getPayDeadlineTime() : 0L);
        return dto;
    }

}

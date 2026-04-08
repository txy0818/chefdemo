package com.txy.chefdemo.service.impl;

import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.PayStatus;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.mapper.ReservationOrderMapper;
import com.txy.chefdemo.service.ChefAvailableTimeService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.utils.DateUtils;
import com.txy.chefdemo.utils.DefaultValueUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    @Autowired
    private ChefAvailableTimeService chefAvailableTimeService;

    @Override
    public int updateById(ReservationOrder order) {
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
        ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
        searchBo.setId(order.getChefAvailableTimeId());
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(searchBo);
        ChefAvailableTime time = CollectionUtils.isEmpty(times) ? null : times.get(0);

        OrderViewDTO dto = new OrderViewDTO();
        dto.setId(ObjectUtils.defaultIfNull(order.getId(), 0L));
        dto.setUserName(ObjectUtils.isNotEmpty(user) && StringUtils.isNotBlank(user.getUsername()) ? user.getUsername() : "-");
        dto.setChefName(ObjectUtils.isNotEmpty(chef) && StringUtils.isNotBlank(chef.getUsername()) ? chef.getUsername() : "-");
        dto.setChefAvailableTimeId(ObjectUtils.defaultIfNull(order.getChefAvailableTimeId(), 0L));
        dto.setChefAvailableStartTime(ObjectUtils.isNotEmpty(time) ? ObjectUtils.defaultIfNull(time.getStartTime(), 0L) : 0L);
        dto.setChefAvailableEndTime(ObjectUtils.isNotEmpty(time) ? ObjectUtils.defaultIfNull(time.getEndTime(), 0L) : 0L);
        dto.setChefAvailableTimeDesc(ObjectUtils.isNotEmpty(time)
                ? DefaultValueUtil.defaultString(DateUtils.format(time.getStartTime(), DateUtils.DATE_TIME_FORMAT))
                + " - "
                + DefaultValueUtil.defaultString(DateUtils.format(time.getEndTime(), DateUtils.DATE_TIME_FORMAT))
                : "-");
        dto.setChefAvailableTimeStatusDesc(ObjectUtils.isNotEmpty(time) && ObjectUtils.isNotEmpty(time.getStatus())
                && ObjectUtils.isNotEmpty(AvailableTimeStatus.getByCode(time.getStatus()))
                ? AvailableTimeStatus.getByCode(time.getStatus()).getDesc() : "-");
        dto.setStartTime(ObjectUtils.defaultIfNull(order.getStartTime(), 0L));
        dto.setStartTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(order.getStartTime(), DateUtils.DATE_TIME_FORMAT)));
        dto.setEndTime(ObjectUtils.defaultIfNull(order.getEndTime(), 0L));
        dto.setEndTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(order.getEndTime(), DateUtils.DATE_TIME_FORMAT)));
        dto.setTotalAmount(ObjectUtils.defaultIfNull(order.getTotalAmount(), 0L));
        dto.setTotalAmountDesc(DefaultValueUtil.formatYuan(order.getTotalAmount()));
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
        dto.setPayDeadlineTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(order.getPayDeadlineTime(), DateUtils.DATE_TIME_FORMAT)));
        return dto;
    }

}

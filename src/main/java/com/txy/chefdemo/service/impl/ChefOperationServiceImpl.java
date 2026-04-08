package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.domain.constant.CuisineType;
import com.txy.chefdemo.domain.constant.Gender;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.dto.ChefAvailableTimeDTO;
import com.txy.chefdemo.domain.dto.ChefProfileDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.AddAvailableTimeReq;
import com.txy.chefdemo.req.ChefOrderActionReq;
import com.txy.chefdemo.req.DeleteAvailableTimeReq;
import com.txy.chefdemo.req.ListAvailableTimesReq;
import com.txy.chefdemo.req.QueryChefOrderReq;
import com.txy.chefdemo.req.QueryOrderDetailReq;
import com.txy.chefdemo.req.SaveChefProfileReq;
import com.txy.chefdemo.resp.ListAvailableTimesResp;
import com.txy.chefdemo.resp.QueryChefOrderResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.ChefAuditRecordService;
import com.txy.chefdemo.service.ChefAvailableTimeService;
import com.txy.chefdemo.service.ChefOperationService;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.OrderFlowService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.transition.order.OrderContext;
import com.txy.chefdemo.transition.order.OrderStateEvent;
import com.txy.chefdemo.utils.DefaultValueUtil;
import com.txy.chefdemo.utils.ObjectMapperUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChefOperationServiceImpl implements ChefOperationService {

    private static final Long DEFAULT_SCORE = 500L;

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
    private ChefAuditRecordService chefAuditRecordService;

    @Override
    public ChefProfileDTO getProfile(Long currentChefId) {
        ChefProfileSearchBo searchBo = new ChefProfileSearchBo();
        searchBo.setUserId(currentChefId);
        List<ChefProfile> chefProfiles = chefProfileService.queryChefListByCondition(searchBo);
        if (CollectionUtils.isEmpty(chefProfiles)) {
            throw new BusinessException("未找到该厨师信息");
        }
        return buildChefProfileDTO(chefProfiles.get(0));
    }

    @Override
    @Transactional
    public void saveProfile(Long currentChefId, SaveChefProfileReq req) {
        long now = System.currentTimeMillis();
        User user = userService.queryById(currentChefId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");

        ChefProfile profile = chefProfileService.queryByUserId(currentChefId);
        ChefAuditRecord pendingRecord = chefAuditRecordService.queryPendingRecordByChefUserId(currentChefId);
        Preconditions.checkArgument(ObjectUtils.isEmpty(pendingRecord), "已有待审核记录，请等待管理员审核");
        if (ObjectUtils.isEmpty(profile)) {
            profile = new ChefProfile();
            profile.setUserId(currentChefId);
            profile.setCreateTime(now);
            profile.setScore(DEFAULT_SCORE);
        } else if (isSameProfile(req, profile)) {
            throw new BusinessException("请勿重复提交");
        }

        profile.setAvatar(user.getAvatar());
        profile.setDisplayName(user.getUsername());
        profile.setPhone(user.getPhone());
        profile.setRealName(req.getRealName());
        profile.setIdCardImgs(ObjectMapperUtils.toJSON(req.getIdCardImgs()));
        profile.setHealthCertImgs(ObjectMapperUtils.toJSON(req.getHealthCertImgs()));
        profile.setChefCertImgs(ObjectMapperUtils.toJSON(req.getChefCertImgs()));
        profile.setCuisineType(ObjectMapperUtils.toJSON(req.getCuisineType()));
        profile.setServiceArea(req.getServiceArea());
        profile.setServiceDesc(req.getServiceDesc());
        profile.setPrice(new BigDecimal(req.getPrice()).multiply(new BigDecimal("100")).longValue());
        profile.setMinPeople(req.getMinPeople());
        profile.setMaxPeople(req.getMaxPeople());
        profile.setAge(req.getAge());
        profile.setGender(req.getGender());
        profile.setWorkYears(req.getWorkYears());
        profile.setUpdateTime(now);
        profile.setAuditStatus(AuditStatus.PENDING.getCode());
        chefProfileService.upsert(profile);

        ChefAuditRecord auditRecord = new ChefAuditRecord();
        auditRecord.setChefUserId(currentChefId);
        auditRecord.setAuditStatus(AuditStatus.PENDING.getCode());
        auditRecord.setCreateTime(now);
        chefAuditRecordService.insert(auditRecord);
    }

    @Override
    @Transactional
    public void addAvailableTime(Long currentChefId, AddAvailableTimeReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req.getStartTime())
                && ObjectUtils.isNotEmpty(req.getEndTime())
                && req.getStartTime() < req.getEndTime(), "时间段非法");
        assertChefAuditApproved(currentChefId);

        ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
        searchBo.setChefId(currentChefId);
        searchBo.setStatusList(AvailableTimeStatus.getValidCodes());
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(searchBo);
        boolean overlap = times.stream()
                .anyMatch(time -> req.getStartTime() < time.getEndTime() && req.getEndTime() > time.getStartTime());
        Preconditions.checkArgument(!overlap, "时间段重叠");

        long now = System.currentTimeMillis();
        ChefAvailableTime time = new ChefAvailableTime(null, currentChefId, req.getStartTime(), req.getEndTime(),
                AvailableTimeStatus.AVAILABLE.getCode(), now, now);
        chefAvailableTimeService.insert(time);
    }

    @Override
    public ListAvailableTimesResp listAvailableTimes(Long currentChefId, ListAvailableTimesReq req) {
        assertChefAuditApproved(currentChefId);
        ChefAvailableTimeSearchBo searchBo = buildTimeSearch(req, currentChefId);
        List<ChefAvailableTime> chefAvailableTimes = chefAvailableTimeService.queryByCondition(searchBo);
        int cnt = chefAvailableTimeService.queryChefListCnt(searchBo);
        List<ChefAvailableTimeDTO> dtos = chefAvailableTimes.stream().map(time -> {
            ChefAvailableTimeDTO dto = new ChefAvailableTimeDTO();
            dto.setId(DefaultValueUtil.defaultLong(time.getId()));
            dto.setStartTime(DefaultValueUtil.defaultLong(time.getStartTime()));
            dto.setEndTime(DefaultValueUtil.defaultLong(time.getEndTime()));
            dto.setStatus(DefaultValueUtil.defaultInteger(time.getStatus()));
            dto.setStatusDesc(DefaultValueUtil.defaultString(AvailableTimeStatus.getByCode(time.getStatus()).getDesc()));
            return dto;
        }).collect(Collectors.toList());
        return new ListAvailableTimesResp(BaseRespConstant.SUC, dtos, cnt);
    }

    @Override
    @Transactional
    public void deleteAvailableTime(Long currentChefId, DeleteAvailableTimeReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && req.getTimeId() != 0L, "时间段不能为空");
        assertChefAuditApproved(currentChefId);
        ChefAvailableTimeSearchBo timeSearchBo = new ChefAvailableTimeSearchBo();
        timeSearchBo.setId(req.getTimeId());
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(timeSearchBo);
        Preconditions.checkArgument(!CollectionUtils.isEmpty(times), BaseRespConstant.FAIL.getDesc());

        ChefAvailableTime time = times.get(0);
        if (!Objects.equals(time.getChefId(), currentChefId)) {
            throw new BusinessException("无权限");
        }

        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setChefId(currentChefId);
        searchBo.setStartTime(time.getStartTime());
        searchBo.setEndTime(time.getEndTime());
        searchBo.setStatuses(OrderStatus.getValidCodes());
        List<ReservationOrder> reservationOrders = reservationOrderService.queryByCondition(searchBo);
        if (!CollectionUtils.isEmpty(reservationOrders)) {
            throw new BusinessException("该时间段已有订单，无法删除");
        }

        time.setStatus(AvailableTimeStatus.DELETED.getCode());
        time.setUpdateTime(System.currentTimeMillis());
        chefAvailableTimeService.updateById(time);
    }

    @Override
    public OrderViewDTO orderDetail(Long currentChefId, QueryOrderDetailReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        assertChefAuditApproved(currentChefId);
        ReservationOrder order = requireChefOrder(currentChefId, req.getOrderId());
        return buildChefOrderView(order);
    }

    @Override
    public QueryChefOrderResp orderList(Long currentChefId, QueryChefOrderReq req) {
        assertChefAuditApproved(currentChefId);
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setChefId(currentChefId);
        if (req.getStatus() != 0) {
            searchBo.setStatus(req.getStatus());
        }
        if (ObjectUtils.isNotEmpty(req.getPage()) && ObjectUtils.isNotEmpty(req.getSize()) && req.getPage() > 0 && req.getSize() > 0) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        List<OrderViewDTO> orderList = reservationOrderService.queryByCondition(searchBo).stream()
                .map(this::buildChefOrderView)
                .collect(Collectors.toList());
        int cnt = reservationOrderService.queryCnt(searchBo);
        return new QueryChefOrderResp(BaseRespConstant.SUC, orderList, cnt);
    }

    @Override
    @Transactional
    public void acceptOrder(Long currentChefId, ChefOrderActionReq req) {
        ReservationOrder order = requireChefOrder(currentChefId, req.getOrderId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_ACCEPT.getCode())) {
            throw new BusinessException(BaseRespConstant.STATUS_ERROR.getDesc());
        }
        orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()), OrderStateEvent.CHEF_ACCEPT,
                new OrderContext(order.getId(), currentChefId, null, req.getReason()));
    }

    @Override
    @Transactional
    public void completeOrder(Long currentChefId, ChefOrderActionReq req) {
        ReservationOrder order = requireChefOrder(currentChefId, req.getOrderId());
        if (!Objects.equals(order.getStatus(), OrderStatus.ACCEPTED.getCode())) {
            throw new BusinessException(BaseRespConstant.STATUS_ERROR.getDesc());
        }
        long now = System.currentTimeMillis();
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(order.getEndTime()) && now >= order.getEndTime(), "未到预约结束时间，暂不能完成订单");
        orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()), OrderStateEvent.COMPLETE,
                new OrderContext(order.getId(), currentChefId, null, req.getReason()));
    }

    @Override
    @Transactional
    public void rejectOrder(Long currentChefId, ChefOrderActionReq req) {
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getReason()), "拒单原因不能为空");
        String reason = req.getReason().trim();
        ReservationOrder order = requireChefOrder(currentChefId, req.getOrderId());
        if (!Objects.equals(order.getStatus(), OrderStatus.PENDING_ACCEPT.getCode())) {
            throw new BusinessException(BaseRespConstant.STATUS_ERROR.getDesc());
        }
        orderFlowService.trigger(OrderStatus.fromCode(order.getStatus()), OrderStateEvent.CHEF_REJECT,
                new OrderContext(order.getId(), currentChefId, null, reason));
    }

    private ChefProfileDTO buildChefProfileDTO(ChefProfile chefProfile) {
        ChefProfileDTO chefProfileDTO = new ChefProfileDTO();
        long score = ObjectUtils.defaultIfNull(chefProfile.getScore(), DEFAULT_SCORE);
        chefProfileDTO.setUserId(DefaultValueUtil.defaultLong(chefProfile.getUserId()));
        chefProfileDTO.setAvatar(StringUtils.isNotBlank(chefProfile.getAvatar()) ? chefProfile.getAvatar() : "-");
        chefProfileDTO.setDisplayName(DefaultValueUtil.defaultString(chefProfile.getDisplayName()));
        chefProfileDTO.setRealName(DefaultValueUtil.defaultString(chefProfile.getRealName()));
        chefProfileDTO.setIdCardImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getIdCardImgs(), String.class)));
        chefProfileDTO.setHealthCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getHealthCertImgs(), String.class)));
        chefProfileDTO.setChefCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getChefCertImgs(), String.class)));
        chefProfileDTO.setCuisineType(DefaultValueUtil.defaultList(CuisineType.fromCodes(ObjectMapperUtils.fromJSONToList(chefProfile.getCuisineType(), Integer.class))));
        chefProfileDTO.setServiceArea(DefaultValueUtil.defaultString(chefProfile.getServiceArea()));
        chefProfileDTO.setServiceDesc(DefaultValueUtil.defaultString(chefProfile.getServiceDesc()));
        chefProfileDTO.setPrice(DefaultValueUtil.defaultLong(chefProfile.getPrice()));
        chefProfileDTO.setMinPeople(DefaultValueUtil.defaultInteger(chefProfile.getMinPeople()));
        chefProfileDTO.setMaxPeople(DefaultValueUtil.defaultInteger(chefProfile.getMaxPeople()));
        chefProfileDTO.setAge(DefaultValueUtil.defaultInteger(chefProfile.getAge()));
        chefProfileDTO.setGender(ObjectUtils.isNotEmpty(Gender.getByCode(chefProfile.getGender())) ? Gender.getByCode(chefProfile.getGender()).getDesc() : "-");
        chefProfileDTO.setWorkYears(DefaultValueUtil.defaultInteger(chefProfile.getWorkYears()));
        chefProfileDTO.setAuditStatus(ObjectUtils.isNotEmpty(AuditStatus.getByCode(chefProfile.getAuditStatus())) ? AuditStatus.getByCode(chefProfile.getAuditStatus()).getDesc() : "-");
        chefProfileDTO.setPhone(StringUtils.isNotBlank(chefProfile.getPhone()) ? chefProfile.getPhone() : "-");
        chefProfileDTO.setScore(DefaultValueUtil.defaultString(
                BigDecimal.valueOf(score).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString()
        ));
        return chefProfileDTO;
    }

    private boolean isSameProfile(SaveChefProfileReq req, ChefProfile profile) {
        Long reqPriceCent = new BigDecimal(req.getPrice()).multiply(new BigDecimal("100")).longValue();
        return req.getRealName().equals(profile.getRealName())
                && ObjectMapperUtils.toJSON(req.getIdCardImgs()).equals(profile.getIdCardImgs())
                && ObjectMapperUtils.toJSON(req.getHealthCertImgs()).equals(profile.getHealthCertImgs())
                && ObjectMapperUtils.toJSON(req.getChefCertImgs()).equals(profile.getChefCertImgs())
                && ObjectMapperUtils.toJSON(req.getCuisineType()).equals(profile.getCuisineType())
                && req.getServiceArea().equals(profile.getServiceArea())
                && req.getServiceDesc().equals(profile.getServiceDesc())
                && reqPriceCent.equals(profile.getPrice())
                && req.getMinPeople().equals(profile.getMinPeople())
                && req.getMaxPeople().equals(profile.getMaxPeople())
                && req.getAge().equals(profile.getAge())
                && req.getGender().equals(profile.getGender())
                && req.getWorkYears().equals(profile.getWorkYears());
    }

    private ChefAvailableTimeSearchBo buildTimeSearch(ListAvailableTimesReq req, Long currentChefId) {
        ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
        searchBo.setChefId(currentChefId);
        if (req.getPage() != 0L && req.getSize() != 0L) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        searchBo.setStatusList(AvailableTimeStatus.getValidCodes());
        return searchBo;
    }

    private OrderViewDTO buildChefOrderView(ReservationOrder order) {
        OrderViewDTO orderViewDTO = reservationOrderService.buildOrderView(order);
        ChefAvailableTimeSearchBo timeSearchBo = new ChefAvailableTimeSearchBo();
        timeSearchBo.setId(order.getChefAvailableTimeId());
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(timeSearchBo);
        ChefAvailableTime time = CollectionUtils.isEmpty(times) ? null : times.get(0);
        orderViewDTO.setChefAvailableTimeId(ObjectUtils.defaultIfNull(order.getChefAvailableTimeId(), 0L));
        orderViewDTO.setChefAvailableStartTime(ObjectUtils.isNotEmpty(time) ? ObjectUtils.defaultIfNull(time.getStartTime(), 0L) : 0L);
        orderViewDTO.setChefAvailableEndTime(ObjectUtils.isNotEmpty(time) ? ObjectUtils.defaultIfNull(time.getEndTime(), 0L) : 0L);
        return orderViewDTO;
    }

    private ReservationOrder requireChefOrder(Long currentChefId, Long orderId) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(orderId), "订单不能为空");
        assertChefAuditApproved(currentChefId);
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(orderId);
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException("订单不存在");
        }
        ReservationOrder order = orders.get(0);
        if (!Objects.equals(order.getChefId(), currentChefId)) {
            throw new BusinessException("无权限");
        }
        return order;
    }

    private void assertChefAuditApproved(Long chefUserId) {
        ChefProfile chefProfile = chefProfileService.queryByUserId(chefUserId);
        if (ObjectUtils.isEmpty(chefProfile) || !Objects.equals(chefProfile.getAuditStatus(), AuditStatus.APPROVED.getCode())) {
            throw new BusinessException(BaseRespConstant.AUDIT_NOT_PASS.getDesc());
        }
    }
}

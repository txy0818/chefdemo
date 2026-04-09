package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.domain.ChefAvailableTime;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.Review;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ChefAvailableTimeSearchBo;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.bo.ReviewSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.AvailableTimeStatus;
import com.txy.chefdemo.domain.constant.CuisineType;
import com.txy.chefdemo.domain.constant.Gender;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.ReviewAuditStatus;
import com.txy.chefdemo.domain.dto.ChefAvailableTimeDTO;
import com.txy.chefdemo.domain.dto.ChefCardDTO;
import com.txy.chefdemo.domain.dto.ChefDetailDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.domain.dto.ReviewDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.ChefDetailReq;
import com.txy.chefdemo.req.ChefReviewDetailReq;
import com.txy.chefdemo.req.ChefTimeDetailReq;
import com.txy.chefdemo.req.QueryOrderDetailReq;
import com.txy.chefdemo.req.QueryUserOrderReq;
import com.txy.chefdemo.req.SearchChefReq;
import com.txy.chefdemo.resp.ChefReviewDetailResp;
import com.txy.chefdemo.resp.ChefTimeDetailResp;
import com.txy.chefdemo.resp.QueryUserOrderResp;
import com.txy.chefdemo.resp.SearchChefResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.ChefAvailableTimeService;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.ReservationOrderService;
import com.txy.chefdemo.service.ReviewService;
import com.txy.chefdemo.service.UserQueryService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.utils.DateUtils;
import com.txy.chefdemo.utils.DefaultValueUtil;
import com.txy.chefdemo.utils.ObjectMapperUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    @Autowired
    private UserService userService;
    @Autowired
    private ChefProfileService chefProfileService;
    @Autowired
    private ChefAvailableTimeService chefAvailableTimeService;
    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private ReviewService reviewService;

    @Override
    public SearchChefResp searchChef(SearchChefReq req) {
        ChefProfileSearchBo searchBo = buildChefSearchReq(req);
        List<ChefProfile> chefProfiles = chefProfileService.queryChefListByCondition(searchBo);
        int cnt = chefProfileService.queryChefListCnt(searchBo);
        List<ChefCardDTO> chefCardDTOS = buildChefCards(chefProfiles);
        return new SearchChefResp(BaseRespConstant.SUC, chefCardDTOS, cnt);
    }

    @Override
    public ChefDetailDTO chefDetail(ChefDetailReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && req.getChefUserId() != 0L, "厨师不能为空");
        ChefProfile profile = chefProfileService.queryByUserId(req.getChefUserId());
        if (ObjectUtils.isEmpty(profile) || !Objects.equals(profile.getAuditStatus(), AuditStatus.APPROVED.getCode())) {
            throw new BusinessException("厨师不存在");
        }

        ChefDetailDTO detail = new ChefDetailDTO();
        detail.setChefUserId(ObjectUtils.defaultIfNull(profile.getUserId(), 0L));
        detail.setAvatar(StringUtils.isNotBlank(profile.getAvatar()) ? profile.getAvatar() : "-");
        detail.setDisplayName(StringUtils.isNotBlank(profile.getDisplayName()) ? profile.getDisplayName() : "-");
        detail.setAge(ObjectUtils.defaultIfNull(profile.getAge(), 0));
        detail.setGender(ObjectUtils.defaultIfNull(profile.getGender(), 0));
        detail.setGenderDesc(ObjectUtils.isNotEmpty(Gender.getByCode(profile.getGender())) ? Gender.getByCode(profile.getGender()).getDesc() : "-");
        detail.setWorkYears(ObjectUtils.defaultIfNull(profile.getWorkYears(), 0));
        detail.setCuisineType(ObjectMapperUtils.fromJSONToList(profile.getCuisineType(), Integer.class));
        detail.setCuisineTypeDesc(CuisineType.fromCodes(ObjectMapperUtils.fromJSONToList(profile.getCuisineType(), Integer.class)));
        detail.setServiceArea(StringUtils.isNotBlank(profile.getServiceArea()) ? profile.getServiceArea() : "-");
        detail.setPrice(ObjectUtils.defaultIfNull(profile.getPrice(), 0L));
        detail.setPriceDesc(DefaultValueUtil.formatYuan(profile.getPrice()));
        detail.setMinPeople(ObjectUtils.defaultIfNull(profile.getMinPeople(), 0));
        detail.setMaxPeople(ObjectUtils.defaultIfNull(profile.getMaxPeople(), 0));
        detail.setServiceDesc(StringUtils.isNotBlank(profile.getServiceDesc()) ? profile.getServiceDesc() : "-");
        detail.setHealthCertImgs(ObjectMapperUtils.fromJSONToList(profile.getHealthCertImgs(), String.class));
        detail.setChefCertImgs(ObjectMapperUtils.fromJSONToList(profile.getChefCertImgs(), String.class));
        detail.setScore(ObjectUtils.isNotEmpty(profile.getScore())
                ? BigDecimal.valueOf(profile.getScore()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue() : 0.0);
        return detail;
    }

    @Override
    public ChefTimeDetailResp chefTimeDetail(ChefTimeDetailReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && req.getChefUserId() != 0L, "厨师不能为空");
        ChefAvailableTimeSearchBo timeSearchBo = buildTimeSearchReq(req);
        List<ChefAvailableTime> times = chefAvailableTimeService.queryByCondition(timeSearchBo);
        int total = chefAvailableTimeService.queryChefListCnt(timeSearchBo);
        List<ChefAvailableTimeDTO> timeDTOS = times.stream().map(time -> {
            ChefAvailableTimeDTO dto = new ChefAvailableTimeDTO();
            dto.setId(ObjectUtils.defaultIfNull(time.getId(), 0L));
            dto.setStartTime(ObjectUtils.defaultIfNull(time.getStartTime(), 0L));
            dto.setStartTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(time.getStartTime(), DateUtils.DATE_TIME_FORMAT)));
            dto.setEndTime(ObjectUtils.defaultIfNull(time.getEndTime(), 0L));
            dto.setEndTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(time.getEndTime(), DateUtils.DATE_TIME_FORMAT)));
            dto.setStatus(ObjectUtils.defaultIfNull(time.getStatus(), 0));
            dto.setStatusDesc(ObjectUtils.isNotEmpty(time.getStatus()) && ObjectUtils.isNotEmpty(AvailableTimeStatus.getByCode(time.getStatus()))
                    ? AvailableTimeStatus.getByCode(time.getStatus()).getDesc() : "-");
            ReservationOrderSearchBo orderSearchBo = new ReservationOrderSearchBo();
            orderSearchBo.setChefAvailableTimeId(time.getId());
            orderSearchBo.setStatuses(OrderStatus.getValidCodes());
            List<String> occupiedTimeDescList = reservationOrderService.queryByCondition(orderSearchBo).stream()
                    .filter(order -> ObjectUtils.isNotEmpty(order.getStartTime()) && ObjectUtils.isNotEmpty(order.getEndTime()))
                    .map(order -> DefaultValueUtil.defaultString(DateUtils.format(order.getStartTime(), DateUtils.DATE_TIME_FORMAT))
                            + " - "
                            + DefaultValueUtil.defaultString(DateUtils.format(order.getEndTime(), DateUtils.DATE_TIME_FORMAT)))
                    .distinct()
                    .collect(Collectors.toList());
            dto.setOccupiedTimeDescList(DefaultValueUtil.defaultList(occupiedTimeDescList));
            return dto;
        }).collect(Collectors.toList());
        return new ChefTimeDetailResp(BaseRespConstant.SUC, timeDTOS, total);
    }

    @Override
    public ChefReviewDetailResp chefReviewDetail(ChefReviewDetailReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && req.getChefUserId() != 0L, "厨师不能为空");
        ReviewSearchBo reviewSearchBo = buildReviewSearchReq(req);
        List<Review> reviews = reviewService.queryByCondition(reviewSearchBo);
        int total = reviewService.queryCnt(reviewSearchBo);
        List<ReviewDTO> reviewDTOS = reviews.stream().map(review -> {
            User user = userService.queryById(review.getUserId());
            User chef = userService.queryById(review.getChefId());

            ReviewDTO dto = new ReviewDTO();
            dto.setId(ObjectUtils.defaultIfNull(review.getId(), 0L));
            dto.setReservationOrderId(ObjectUtils.defaultIfNull(review.getReservationOrderId(), 0L));
            dto.setUserName(ObjectUtils.isNotEmpty(user) && StringUtils.isNotBlank(user.getUsername()) ? user.getUsername() : "-");
            dto.setChefName(ObjectUtils.isNotEmpty(chef) && StringUtils.isNotBlank(chef.getUsername()) ? chef.getUsername() : "-");
            dto.setScore(ObjectUtils.isNotEmpty(review.getScore()) ? String.valueOf(review.getScore() / 100.0) : "-");
            dto.setContent(StringUtils.isNotBlank(review.getContent()) ? review.getContent() : "-");
            dto.setAuditStatus(ObjectUtils.defaultIfNull(review.getAuditStatus(), 0));
            dto.setAuditStatusDesc(ObjectUtils.isNotEmpty(review.getAuditStatus()) && ObjectUtils.isNotEmpty(ReviewAuditStatus.getByCode(review.getAuditStatus()))
                    ? ReviewAuditStatus.getByCode(review.getAuditStatus()).getDesc() : "-");
            dto.setAuditReason(StringUtils.isNotBlank(review.getAuditReason()) ? review.getAuditReason() : "-");
            dto.setCreateTime(ObjectUtils.defaultIfNull(review.getCreateTime(), 0L));
            dto.setCreateTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(review.getCreateTime(), DateUtils.DATE_TIME_FORMAT)));
            return dto;
        }).collect(Collectors.toList());
        return new ChefReviewDetailResp(BaseRespConstant.SUC, reviewDTOS, total);
    }

    @Override
    public QueryUserOrderResp orderList(Long currentUserId, QueryUserOrderReq req) {
        ReservationOrderSearchBo searchBo = buildOrderListReq(req, currentUserId);
        List<OrderViewDTO> list = reservationOrderService.queryByCondition(searchBo).stream()
                .map(reservationOrderService::buildOrderView)
                .collect(Collectors.toList());
        int cnt = reservationOrderService.queryCnt(searchBo);
        return new QueryUserOrderResp(BaseRespConstant.SUC, list, cnt);
    }

    @Override
    public OrderViewDTO orderDetail(Long currentUserId, QueryOrderDetailReq req) {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req) && ObjectUtils.isNotEmpty(req.getOrderId()), "订单不能为空");
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        searchBo.setOrderId(req.getOrderId());
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);
        if (CollectionUtils.isEmpty(orders)) {
            throw new BusinessException("订单不存在");
        }
        ReservationOrder order = orders.get(0);
        if (!Objects.equals(order.getUserId(), currentUserId)) {
            throw new BusinessException("无权限");
        }
        return reservationOrderService.buildOrderView(order);
    }

    private ChefProfileSearchBo buildChefSearchReq(SearchChefReq req) {
        ChefProfileSearchBo searchBo = new ChefProfileSearchBo();
        if (!CollectionUtils.isEmpty(req.getCuisineTypeList())) {
            searchBo.setCuisineTypeList(req.getCuisineTypeList());
        }
        if (StringUtils.isNotBlank(req.getServiceArea())) {
            searchBo.setServiceArea(req.getServiceArea());
        }
        if (StringUtils.isNotBlank(req.getKeyword())) {
            searchBo.setUserName(req.getKeyword());
        }
        if (req.getPage() != 0L && req.getSize() != 0L) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        if (req.getMinPrice() != 0L) {
            searchBo.setMinPrice(req.getMinPrice());
        }
        if (req.getMaxPrice() != 0L) {
            searchBo.setMaxPrice(req.getMaxPrice());
        }
        if (StringUtils.isNotBlank(req.getSortType())) {
            searchBo.setSortType(req.getSortType());
        }
        if (req.getMinScore() != 0.0d) {
            searchBo.setMinScore(Math.round(req.getMinScore() * 100));
        }
        if (req.getMaxScore() != 0.0d) {
            searchBo.setMaxScore(Math.round(req.getMaxScore() * 100));
        }
        searchBo.setAuditStatus(AuditStatus.APPROVED.getCode());
        return searchBo;
    }

    private List<ChefCardDTO> buildChefCards(List<ChefProfile> chefProfiles) {
        return chefProfiles.stream().map(chefProfile -> {
            ChefCardDTO chefCardDTO = new ChefCardDTO();
            chefCardDTO.setChefUserId(ObjectUtils.defaultIfNull(chefProfile.getUserId(), 0L));
            chefCardDTO.setAvatar(StringUtils.isNotBlank(chefProfile.getAvatar()) ? chefProfile.getAvatar() : "-");
            chefCardDTO.setDisplayName(StringUtils.isNotBlank(chefProfile.getDisplayName()) ? chefProfile.getDisplayName() : "-");
            chefCardDTO.setCuisineType(ObjectMapperUtils.fromJSONToList(chefProfile.getCuisineType(), Integer.class));
            chefCardDTO.setCuisineTypeDesc(CuisineType.fromCodes(ObjectMapperUtils.fromJSONToList(chefProfile.getCuisineType(), Integer.class)));
            chefCardDTO.setServiceArea(StringUtils.isNotBlank(chefProfile.getServiceArea()) ? chefProfile.getServiceArea() : "-");
            chefCardDTO.setPrice(ObjectUtils.defaultIfNull(chefProfile.getPrice(), 0L));
            chefCardDTO.setPriceDesc(DefaultValueUtil.formatYuan(chefProfile.getPrice()));
            chefCardDTO.setScore(ObjectUtils.isNotEmpty(chefProfile.getScore())
                    ? BigDecimal.valueOf(chefProfile.getScore()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue() : 0.0);
            return chefCardDTO;
        }).collect(Collectors.toList());
    }

    private ChefAvailableTimeSearchBo buildTimeSearchReq(ChefTimeDetailReq req) {
        ChefAvailableTimeSearchBo searchBo = new ChefAvailableTimeSearchBo();
        searchBo.setChefId(req.getChefUserId());
        if (ObjectUtils.isNotEmpty(req.getPage()) && ObjectUtils.isNotEmpty(req.getSize()) && req.getPage() > 0 && req.getSize() > 0) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        searchBo.setStatusList(AvailableTimeStatus.getValidCodes());
        return searchBo;
    }

    private ReviewSearchBo buildReviewSearchReq(ChefReviewDetailReq req) {
        ReviewSearchBo searchBo = new ReviewSearchBo();
        searchBo.setChefId(req.getChefUserId());
        if (ObjectUtils.isNotEmpty(req.getPage()) && ObjectUtils.isNotEmpty(req.getSize()) && req.getPage() > 0 && req.getSize() > 0) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        searchBo.setAuditStatus(ReviewAuditStatus.APPROVED.getCode());
        return searchBo;
    }

    private ReservationOrderSearchBo buildOrderListReq(QueryUserOrderReq req, Long currentUserId) {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        if (req.getStatus() != 0) {
            searchBo.setStatus(req.getStatus());
        }
        if (req.getPage() != 0L && req.getSize() != 0L) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        searchBo.setUserId(currentUserId);
        return searchBo;
    }
}

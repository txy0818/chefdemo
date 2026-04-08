package com.txy.chefdemo.service.impl;

import com.google.common.collect.Lists;
import com.txy.chefdemo.domain.ChefAuditRecord;
import com.txy.chefdemo.domain.ChefProfile;
import com.txy.chefdemo.domain.ChefProfileChange;
import com.txy.chefdemo.domain.ReservationOrder;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.bo.ChefProfileSearchBo;
import com.txy.chefdemo.domain.bo.ReservationOrderSearchBo;
import com.txy.chefdemo.domain.bo.UserSearchBo;
import com.txy.chefdemo.domain.constant.AuditStatus;
import com.txy.chefdemo.domain.constant.CuisineType;
import com.txy.chefdemo.domain.constant.Gender;
import com.txy.chefdemo.domain.constant.OrderStatus;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.domain.dto.ChefProfileDTO;
import com.txy.chefdemo.domain.dto.OrderStatisticsDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.domain.dto.UserDTO;
import com.txy.chefdemo.req.QueryAuditChefReq;
import com.txy.chefdemo.req.QueryChefReq;
import com.txy.chefdemo.req.QueryUserListReq;
import com.txy.chefdemo.req.QueryUserOrderReq;
import com.txy.chefdemo.resp.QueryAuditChefResp;
import com.txy.chefdemo.resp.QueryChefResp;
import com.txy.chefdemo.resp.QueryUserListResp;
import com.txy.chefdemo.resp.QueryUserOrderResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AdminQueryService;
import com.txy.chefdemo.service.ChefAuditRecordService;
import com.txy.chefdemo.service.ChefProfileChangeService;
import com.txy.chefdemo.service.ChefProfileService;
import com.txy.chefdemo.service.ReservationOrderService;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminQueryServiceImpl implements AdminQueryService {

    private static final Long DEFAULT_SCORE = 500L;

    @Autowired
    private UserService userService;
    @Autowired
    private ChefProfileService chefProfileService;
    @Autowired
    private ReservationOrderService reservationOrderService;
    @Autowired
    private ChefAuditRecordService chefAuditRecordService;
    @Autowired
    private ChefProfileChangeService chefProfileChangeService;

    @Override
    public QueryChefResp queryChefList(QueryChefReq req) {
        ChefProfileSearchBo searchBo = buildChefSearch(req);
        List<ChefProfile> chefProfileList = chefProfileService.queryChefListByCondition(searchBo);
        int cnt = chefProfileService.queryChefListCnt(searchBo);
        return new QueryChefResp(BaseRespConstant.SUC, buildChefList(chefProfileList), cnt);
    }

    @Override
    public QueryUserListResp queryUserList(QueryUserListReq req) {
        UserSearchBo userSearchBo = buildUserSearch(req);
        List<User> userList = userService.queryUserListByCondition(userSearchBo);
        int cnt = userService.queryUserListCnt(userSearchBo);
        return new QueryUserListResp(BaseRespConstant.SUC, buildUserList(userList), cnt);
    }

    @Override
    public QueryAuditChefResp queryAuditChef(QueryAuditChefReq req) {
        List<ChefAuditRecord> chefAuditRecords = chefAuditRecordService.queryPendingRecord();
        if (CollectionUtils.isEmpty(chefAuditRecords)) {
            return new QueryAuditChefResp(BaseRespConstant.SUC, Lists.newArrayList(), 0);
        }
        List<Long> chefIds = chefAuditRecords.stream().map(ChefAuditRecord::getChefUserId).collect(Collectors.toList());
        List<User> userList = userService.queryByIdList(chefIds);
        if (CollectionUtils.isEmpty(userList)) {
            return new QueryAuditChefResp(BaseRespConstant.SUC, Lists.newArrayList(), 0);
        }
        List<User> normalUserList = userList.stream()
                .filter(user -> user.getStatus() == UserStatus.NORMAL.getCode())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(normalUserList)) {
            return new QueryAuditChefResp(BaseRespConstant.SUC, Lists.newArrayList(), 0);
        }
        ChefProfileSearchBo searchBo = new ChefProfileSearchBo();
        if (req.getPage() > 0L && req.getSize() > 0L) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        searchBo.setUserIdList(normalUserList.stream().map(User::getId).collect(Collectors.toList()));
        List<ChefProfile> chefProfileList = chefProfileService.queryChefListByCondition(searchBo);
        int cnt = chefProfileService.queryChefListCnt(searchBo);
        return new QueryAuditChefResp(BaseRespConstant.SUC, buildAuditChefList(chefProfileList), cnt);
    }

    @Override
    public QueryUserOrderResp orderList(QueryUserOrderReq req) {
        ReservationOrderSearchBo searchBo = buildOrderSearch(req);
        List<OrderViewDTO> orderList = reservationOrderService.queryByCondition(searchBo).stream()
                .map(reservationOrderService::buildOrderView)
                .collect(Collectors.toList());
        int cnt = reservationOrderService.queryCnt(searchBo);
        return new QueryUserOrderResp(BaseRespConstant.SUC, orderList, cnt);
    }

    @Override
    public OrderStatisticsDTO orderStatistics() {
        ReservationOrderSearchBo searchBo = new ReservationOrderSearchBo();
        long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        // 7天前的0点0分
        LocalDate startDate = LocalDate.now().minusDays(7);
        long start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        searchBo.setStartTime(start);
        searchBo.setEndTime(System.currentTimeMillis());
        List<ReservationOrder> orders = reservationOrderService.queryByCondition(searchBo);

        long total = orders.size();
        long pendingPayment = orders.stream().filter(order -> order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode()).count();
        long pendingAccept = orders.stream().filter(o -> Objects.equals(o.getStatus(), OrderStatus.PENDING_ACCEPT.getCode())).count();
        long accepted = orders.stream().filter(o -> Objects.equals(o.getStatus(), OrderStatus.ACCEPTED.getCode())).count();
        long rejected = orders.stream().filter(o -> Objects.equals(o.getStatus(), OrderStatus.REJECTED.getCode())).count();
        long completed = orders.stream().filter(o -> Objects.equals(o.getStatus(), OrderStatus.COMPLETED.getCode())).count();
        long cancelled = orders.stream().filter(o -> Objects.equals(o.getStatus(), OrderStatus.CANCELLED.getCode())).count();

        List<ReservationOrder> todayOrders = orders.stream()
                .filter(o -> o.getCreateTime() >= todayStart)
                .collect(Collectors.toList());
        List<OrderStatisticsDTO.PieItem> pie = Arrays.asList(
                new OrderStatisticsDTO.PieItem(OrderStatus.PENDING_PAYMENT.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.PENDING_PAYMENT.getCode())).count()),
                new OrderStatisticsDTO.PieItem(OrderStatus.PENDING_ACCEPT.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.PENDING_ACCEPT.getCode())).count()),
                new OrderStatisticsDTO.PieItem(OrderStatus.ACCEPTED.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.ACCEPTED.getCode())).count()),
                new OrderStatisticsDTO.PieItem(OrderStatus.REJECTED.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.REJECTED.getCode())).count()),
                new OrderStatisticsDTO.PieItem(OrderStatus.COMPLETED.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.COMPLETED.getCode())).count()),
                new OrderStatisticsDTO.PieItem(OrderStatus.CANCELLED.getDesc(), todayOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.CANCELLED.getCode())).count())
        );

        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate localDate = LocalDate.now().minusDays(i);
            long startTime = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long count = orders.stream()
                    .filter(o -> o.getCreateTime() >= startTime && o.getCreateTime() < endTime)
                    .count();
            dateCountMap.put(localDate.format(DateTimeFormatter.ofPattern("MM-dd")), count);
        }

        return new OrderStatisticsDTO(
                total,
                pendingPayment,
                pendingAccept,
                accepted,
                completed,
                cancelled,
                rejected,
                pie,
                new ArrayList<>(dateCountMap.keySet()),
                new ArrayList<>(dateCountMap.values())
        );
    }

    private ChefProfileSearchBo buildChefSearch(QueryChefReq req) {
        ChefProfileSearchBo searchBo = new ChefProfileSearchBo();
        if (req.getAuditStatus() != 0) {
            searchBo.setAuditStatus(ObjectUtils.isNotEmpty(AuditStatus.getByCode(req.getAuditStatus())) ? req.getAuditStatus() : 0);
        }
        if (req.getUserId() != 0L) {
            searchBo.setUserId(req.getUserId());
        }
        if (StringUtils.isNotBlank(req.getUsername())) {
            searchBo.setUserName(req.getUsername());
        }
        if (req.getPage() != 0L && req.getSize() != 0L) {
            searchBo.setOffset((req.getPage() - 1) * req.getSize());
            searchBo.setSize(req.getSize());
        } else {
            searchBo.setOffset(0L);
            searchBo.setSize(10L);
        }
        return searchBo;
    }

    private UserSearchBo buildUserSearch(QueryUserListReq req) {
        UserSearchBo userSearchBo = new UserSearchBo();
        if (req.getRole() != 0) {
            userSearchBo.setRole(ObjectUtils.isNotEmpty(UserRole.getByCode(req.getRole())) ? req.getRole() : 0);
        }
        if (req.getUserId() != 0L) {
            userSearchBo.setUserId(req.getUserId());
        }
        if (StringUtils.isNotBlank(req.getUsername())) {
            userSearchBo.setUserName(req.getUsername());
        }
        if (req.getStatus() != 0) {
            userSearchBo.setStatus(ObjectUtils.isNotEmpty(UserStatus.getByCode(req.getStatus())) ? req.getStatus() : 0);
        }
        if (req.getPage() != 0L && req.getSize() != 0L) {
            userSearchBo.setOffset((req.getPage() - 1) * req.getSize());
            userSearchBo.setSize(req.getSize());
        } else {
            userSearchBo.setOffset(0L);
            userSearchBo.setSize(10L);
        }
        return userSearchBo;
    }

    private ReservationOrderSearchBo buildOrderSearch(QueryUserOrderReq req) {
        ReservationOrderSearchBo reservationOrderSearchBo = new ReservationOrderSearchBo();
        if (req.getStatus() != 0) {
            reservationOrderSearchBo.setStatus(req.getStatus());
        }
        if (req.getPage() != 0L && req.getSize() != 0L) {
            reservationOrderSearchBo.setOffset((req.getPage() - 1) * req.getSize());
            reservationOrderSearchBo.setSize(req.getSize());
        } else {
            reservationOrderSearchBo.setOffset(0L);
            reservationOrderSearchBo.setSize(10L);
        }
        return reservationOrderSearchBo;
    }

    private List<ChefProfileDTO> buildChefList(List<ChefProfile> chefProfileList) {
        return chefProfileList.stream().map(chefProfile -> {
            ChefProfileDTO chefProfileDTO = new ChefProfileDTO();
            long score = ObjectUtils.defaultIfNull(chefProfile.getScore(), DEFAULT_SCORE);
            chefProfileDTO.setUserId(DefaultValueUtil.defaultLong(chefProfile.getUserId()));
            chefProfileDTO.setAvatar(StringUtils.isNotBlank(chefProfile.getAvatar()) ? chefProfile.getAvatar() : "-");
            chefProfileDTO.setDisplayName(DefaultValueUtil.defaultString(chefProfile.getDisplayName()));
            chefProfileDTO.setRealName(DefaultValueUtil.defaultString(chefProfile.getRealName()));
            chefProfileDTO.setIdCardImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getIdCardImgs(), String.class)));
            chefProfileDTO.setHealthCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getHealthCertImgs(), String.class)));
            chefProfileDTO.setChefCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getChefCertImgs(), String.class)));
            chefProfileDTO.setCuisineType(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefProfile.getCuisineType(), Integer.class)));
            chefProfileDTO.setCuisineTypeDesc(DefaultValueUtil.defaultList(CuisineType.fromCodes(ObjectMapperUtils.fromJSONToList(chefProfile.getCuisineType(), Integer.class))));
            chefProfileDTO.setServiceArea(DefaultValueUtil.defaultString(chefProfile.getServiceArea()));
            chefProfileDTO.setServiceDesc(DefaultValueUtil.defaultString(chefProfile.getServiceDesc()));
            chefProfileDTO.setPrice(DefaultValueUtil.defaultLong(chefProfile.getPrice()));
            chefProfileDTO.setPriceDesc(DefaultValueUtil.formatYuan(chefProfile.getPrice()));
            chefProfileDTO.setMinPeople(DefaultValueUtil.defaultInteger(chefProfile.getMinPeople()));
            chefProfileDTO.setMaxPeople(DefaultValueUtil.defaultInteger(chefProfile.getMaxPeople()));
            chefProfileDTO.setAge(DefaultValueUtil.defaultInteger(chefProfile.getAge()));
            chefProfileDTO.setGender(DefaultValueUtil.defaultInteger(chefProfile.getGender()));
            chefProfileDTO.setGenderDesc(ObjectUtils.isNotEmpty(Gender.getByCode(chefProfile.getGender())) ? Gender.getByCode(chefProfile.getGender()).getDesc() : "-");
            chefProfileDTO.setWorkYears(DefaultValueUtil.defaultInteger(chefProfile.getWorkYears()));
            chefProfileDTO.setAuditStatus(DefaultValueUtil.defaultInteger(chefProfile.getAuditStatus()));
            chefProfileDTO.setAuditStatusDesc(ObjectUtils.isNotEmpty(AuditStatus.getByCode(chefProfile.getAuditStatus())) ? AuditStatus.getByCode(chefProfile.getAuditStatus()).getDesc() : "-");
            chefProfileDTO.setPendingAuditStatus(0);
            chefProfileDTO.setPendingAuditStatusDesc("-");
            chefProfileDTO.setPendingRejectReason("-");
            chefProfileDTO.setPhone(StringUtils.isNotBlank(chefProfile.getPhone()) ? chefProfile.getPhone() : "-");
            chefProfileDTO.setScore(DefaultValueUtil.defaultString(BigDecimal.valueOf(score).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString()));
            return chefProfileDTO;
        }).collect(Collectors.toList());
    }

    private List<ChefProfileDTO> buildAuditChefList(List<ChefProfile> chefProfileList) {
        return chefProfileList.stream().map(chefProfile -> {
            ChefProfileChange change = chefProfileChangeService.queryByUserId(chefProfile.getUserId());
            return buildChefProfileDTO(
                    ObjectUtils.isNotEmpty(change) && Objects.equals(change.getAuditStatus(), AuditStatus.PENDING.getCode()) ? change : null,
                    chefProfile
            );
        }).collect(Collectors.toList());
    }

    private ChefProfileDTO buildChefProfileDTO(ChefProfileChange change, ChefProfile profile) {
        ChefProfileDTO dto = new ChefProfileDTO();
        long score = ObjectUtils.defaultIfNull(profile.getScore(), DEFAULT_SCORE);
        boolean useChange = ObjectUtils.isNotEmpty(change);
        String avatar = useChange ? change.getAvatar() : profile.getAvatar();
        String displayName = useChange ? change.getDisplayName() : profile.getDisplayName();
        String realName = useChange ? change.getRealName() : profile.getRealName();
        String idCardImgs = useChange ? change.getIdCardImgs() : profile.getIdCardImgs();
        String healthCertImgs = useChange ? change.getHealthCertImgs() : profile.getHealthCertImgs();
        String chefCertImgs = useChange ? change.getChefCertImgs() : profile.getChefCertImgs();
        String cuisineType = useChange ? change.getCuisineType() : profile.getCuisineType();
        String serviceArea = useChange ? change.getServiceArea() : profile.getServiceArea();
        String serviceDesc = useChange ? change.getServiceDesc() : profile.getServiceDesc();
        Long price = useChange ? change.getPrice() : profile.getPrice();
        Integer minPeople = useChange ? change.getMinPeople() : profile.getMinPeople();
        Integer maxPeople = useChange ? change.getMaxPeople() : profile.getMaxPeople();
        Integer age = useChange ? change.getAge() : profile.getAge();
        Integer gender = useChange ? change.getGender() : profile.getGender();
        Integer workYears = useChange ? change.getWorkYears() : profile.getWorkYears();
        String phone = useChange ? change.getPhone() : profile.getPhone();
        Integer auditStatus = useChange ? change.getAuditStatus() : profile.getAuditStatus();
        dto.setUserId(DefaultValueUtil.defaultLong(profile.getUserId()));
        dto.setAvatar(StringUtils.isNotBlank(avatar) ? avatar : "-");
        dto.setDisplayName(DefaultValueUtil.defaultString(displayName));
        dto.setRealName(DefaultValueUtil.defaultString(realName));
        dto.setIdCardImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(idCardImgs, String.class)));
        dto.setHealthCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(healthCertImgs, String.class)));
        dto.setChefCertImgs(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(chefCertImgs, String.class)));
        dto.setCuisineType(DefaultValueUtil.defaultList(ObjectMapperUtils.fromJSONToList(cuisineType, Integer.class)));
        dto.setCuisineTypeDesc(DefaultValueUtil.defaultList(CuisineType.fromCodes(ObjectMapperUtils.fromJSONToList(cuisineType, Integer.class))));
        dto.setServiceArea(DefaultValueUtil.defaultString(serviceArea));
        dto.setServiceDesc(DefaultValueUtil.defaultString(serviceDesc));
        dto.setPrice(DefaultValueUtil.defaultLong(price));
        dto.setPriceDesc(DefaultValueUtil.formatYuan(price));
        dto.setMinPeople(DefaultValueUtil.defaultInteger(minPeople));
        dto.setMaxPeople(DefaultValueUtil.defaultInteger(maxPeople));
        dto.setAge(DefaultValueUtil.defaultInteger(age));
        dto.setGender(DefaultValueUtil.defaultInteger(gender));
        dto.setGenderDesc(ObjectUtils.isNotEmpty(Gender.getByCode(gender)) ? Gender.getByCode(gender).getDesc() : "-");
        dto.setWorkYears(DefaultValueUtil.defaultInteger(workYears));
        dto.setAuditStatus(DefaultValueUtil.defaultInteger(auditStatus));
        dto.setAuditStatusDesc(ObjectUtils.isNotEmpty(AuditStatus.getByCode(auditStatus)) ? AuditStatus.getByCode(auditStatus).getDesc() : "-");
        dto.setPendingAuditStatus(useChange ? DefaultValueUtil.defaultInteger(change.getAuditStatus()) : 0);
        dto.setPendingAuditStatusDesc(useChange && ObjectUtils.isNotEmpty(AuditStatus.getByCode(change.getAuditStatus()))
                ? AuditStatus.getByCode(change.getAuditStatus()).getDesc() : "-");
        dto.setPendingRejectReason(useChange ? DefaultValueUtil.defaultString(change.getRejectReason()) : "-");
        dto.setPhone(StringUtils.isNotBlank(phone) ? phone : "-");
        dto.setScore(DefaultValueUtil.defaultString(
                BigDecimal.valueOf(score).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).toString()
        ));
        return dto;
    }

    private List<UserDTO> buildUserList(List<User> userList) {
        return userList.stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(DefaultValueUtil.defaultLong(user.getId()));
            userDTO.setUsername(DefaultValueUtil.defaultString(user.getUsername()));
            userDTO.setRole(DefaultValueUtil.defaultInteger(user.getRole()));
            userDTO.setRoleDesc(ObjectUtils.isNotEmpty(UserRole.getByCode(user.getRole())) ? UserRole.getByCode(user.getRole()).getDesc() : "-");
            userDTO.setAvatar(DefaultValueUtil.defaultString(user.getAvatar()));
            userDTO.setPhone(StringUtils.isNotBlank(user.getPhone()) ? user.getPhone() : "-");
            userDTO.setStatus(DefaultValueUtil.defaultInteger(user.getStatus()));
            userDTO.setStatusDesc(ObjectUtils.isNotEmpty(UserStatus.getByCode(user.getStatus())) ? UserStatus.getByCode(user.getStatus()).getDesc() : "-");
            userDTO.setLastLoginTime(DefaultValueUtil.defaultLong(user.getLastLoginTime()));
            userDTO.setLastLoginTimeDesc(DefaultValueUtil.defaultString(DateUtils.format(user.getLastLoginTime(), DateUtils.DATE_TIME_FORMAT)));
            return userDTO;
        }).collect(Collectors.toList());
    }
}

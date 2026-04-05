package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.dto.OrderStatisticsDTO;
import com.txy.chefdemo.req.QueryAuditChefReq;
import com.txy.chefdemo.req.QueryChefReq;
import com.txy.chefdemo.req.QueryUserListReq;
import com.txy.chefdemo.req.QueryUserOrderReq;
import com.txy.chefdemo.resp.QueryAuditChefResp;
import com.txy.chefdemo.resp.QueryChefResp;
import com.txy.chefdemo.resp.QueryUserListResp;
import com.txy.chefdemo.resp.QueryUserOrderResp;

public interface AdminQueryService {

    QueryChefResp queryChefList(QueryChefReq req);

    QueryUserListResp queryUserList(QueryUserListReq req);

    QueryAuditChefResp queryAuditChef(QueryAuditChefReq req);

    QueryUserOrderResp orderList(QueryUserOrderReq req);

    OrderStatisticsDTO orderStatistics();
}

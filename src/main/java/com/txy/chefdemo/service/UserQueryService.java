package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.dto.ChefDetailDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
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

public interface UserQueryService {

    SearchChefResp searchChef(SearchChefReq req);

    ChefDetailDTO chefDetail(ChefDetailReq req);

    ChefTimeDetailResp chefTimeDetail(ChefTimeDetailReq req);

    ChefReviewDetailResp chefReviewDetail(ChefReviewDetailReq req);

    QueryUserOrderResp orderList(Long currentUserId, QueryUserOrderReq req);

    OrderViewDTO orderDetail(Long currentUserId, QueryOrderDetailReq req);
}

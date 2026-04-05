package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.dto.ChefProfileDTO;
import com.txy.chefdemo.domain.dto.OrderViewDTO;
import com.txy.chefdemo.req.AddAvailableTimeReq;
import com.txy.chefdemo.req.ChefOrderActionReq;
import com.txy.chefdemo.req.DeleteAvailableTimeReq;
import com.txy.chefdemo.req.ListAvailableTimesReq;
import com.txy.chefdemo.req.QueryChefOrderReq;
import com.txy.chefdemo.req.QueryOrderDetailReq;
import com.txy.chefdemo.req.SaveChefProfileReq;
import com.txy.chefdemo.resp.ListAvailableTimesResp;
import com.txy.chefdemo.resp.QueryChefOrderResp;

public interface ChefOperationService {

    ChefProfileDTO getProfile(Long currentChefId);

    void saveProfile(Long currentChefId, SaveChefProfileReq req);

    void addAvailableTime(Long currentChefId, AddAvailableTimeReq req);

    ListAvailableTimesResp listAvailableTimes(Long currentChefId, ListAvailableTimesReq req);

    void deleteAvailableTime(Long currentChefId, DeleteAvailableTimeReq req);

    OrderViewDTO orderDetail(Long currentChefId, QueryOrderDetailReq req);

    QueryChefOrderResp orderList(Long currentChefId, QueryChefOrderReq req);

    void acceptOrder(Long currentChefId, ChefOrderActionReq req);

    void completeOrder(Long currentChefId, ChefOrderActionReq req);

    void rejectOrder(Long currentChefId, ChefOrderActionReq req);
}

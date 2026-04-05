package com.txy.chefdemo.service;

import com.txy.chefdemo.req.LogInReq;
import com.txy.chefdemo.req.RegisterReq;
import com.txy.chefdemo.resp.LogInResp;
import com.txy.chefdemo.resp.RegisterResp;

public interface AuthOperationService {

    RegisterResp register(RegisterReq registerReq);

    LogInResp login(LogInReq logInReq);

    void logout(Long currentUserId, String authorizationHeader);
}

package com.txy.chefdemo.service;

import com.txy.chefdemo.domain.dto.UserDTO;
import com.txy.chefdemo.req.UpdatePasswordReq;
import com.txy.chefdemo.req.UpdateProfileReq;

public interface ProfileOperationService {

    UserDTO queryInfo(Long currentUserId);

    void updateUserInfo(Long currentUserId, UpdateProfileReq req);

    void updatePassword(Long currentUserId, UpdatePasswordReq req);
}

package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.domain.dto.UserDTO;
import com.txy.chefdemo.exp.BusinessException;
import com.txy.chefdemo.req.UpdatePasswordReq;
import com.txy.chefdemo.req.UpdateProfileReq;
import com.txy.chefdemo.service.ProfileOperationService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.utils.DateUtils;
import com.txy.chefdemo.utils.DefaultValueUtil;
import com.txy.chefdemo.utils.PasswordUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileOperationServiceImpl implements ProfileOperationService {

    @Autowired
    private UserService userService;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public UserDTO queryInfo(Long currentUserId) {
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");
        return buildUserDTO(user);
    }

    @Override
    @Transactional
    public void updateUserInfo(Long currentUserId, UpdateProfileReq req) {
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");
        if (StringUtils.isBlank(req.getAvatar()) && StringUtils.isBlank(user.getAvatar())
                && StringUtils.isNotBlank(req.getPhone()) && req.getPhone().equals(user.getPhone())) {
            throw new BusinessException("用户信息未改变");
        }
        if (StringUtils.isNotBlank(req.getAvatar()) && req.getAvatar().equals(user.getAvatar())
                && StringUtils.isNotBlank(req.getPhone()) && req.getPhone().equals(user.getPhone())) {
            throw new BusinessException("用户信息未改变");
        }
        if (StringUtils.isNotBlank(req.getAvatar()) && !req.getAvatar().equals(user.getAvatar())) {
            user.setAvatar(req.getAvatar());
        }
        if (StringUtils.isNotBlank(req.getPhone()) && !req.getPhone().equals(user.getPhone())) {
            user.setPhone(req.getPhone());
        }
        user.setUpdateTime(System.currentTimeMillis());
        userService.upsert(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long currentUserId, UpdatePasswordReq req) {
        User user = userService.queryById(currentUserId);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(user), "用户不存在");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getOldPassword()), "旧密码不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getNewPassword()), "新密码不能为空");

        String oldEncryptedPwd = PasswordUtil.sha256(req.getOldPassword());
        if (!oldEncryptedPwd.equals(user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        String newEncryptedPwd = PasswordUtil.sha256(req.getNewPassword());
        if (newEncryptedPwd.equals(oldEncryptedPwd)) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        user.setPassword(newEncryptedPwd);
        user.setUpdateTime(System.currentTimeMillis());
        userService.upsert(user);
        redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + user.getId()).delete();
    }

    private UserDTO buildUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DefaultValueUtil.defaultLong(user.getId()));
        userDTO.setUsername(DefaultValueUtil.defaultString(user.getUsername()));
        userDTO.setRole(ObjectUtils.isNotEmpty(UserRole.getByCode(user.getRole())) ? UserRole.getByCode(user.getRole()).getDesc() : "-");
        userDTO.setAvatar(DefaultValueUtil.defaultString(user.getAvatar()));
        userDTO.setPhone(StringUtils.isNotBlank(user.getPhone()) ? user.getPhone() : "-");
        userDTO.setStatus(ObjectUtils.isNotEmpty(UserStatus.getByCode(user.getStatus())) ? UserStatus.getByCode(user.getStatus()).getDesc() : "-");
        userDTO.setLastLoginTime(DefaultValueUtil.defaultString(DateUtils.format(user.getLastLoginTime(), DateUtils.DATE_TIME_FORMAT)));
        return userDTO;
    }
}

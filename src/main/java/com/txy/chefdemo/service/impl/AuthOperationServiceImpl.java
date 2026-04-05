package com.txy.chefdemo.service.impl;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.User;
import com.txy.chefdemo.domain.UserStatusRecord;
import com.txy.chefdemo.domain.Wallet;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.req.LogInReq;
import com.txy.chefdemo.req.RegisterReq;
import com.txy.chefdemo.resp.BaseResp;
import com.txy.chefdemo.resp.LogInResp;
import com.txy.chefdemo.resp.RegisterResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.service.AuthOperationService;
import com.txy.chefdemo.service.FrozenChefCleanupService;
import com.txy.chefdemo.service.UserService;
import com.txy.chefdemo.service.UserStatusRecordService;
import com.txy.chefdemo.service.WalletService;
import com.txy.chefdemo.utils.JWTUtil;
import com.txy.chefdemo.utils.PasswordUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AuthOperationServiceImpl implements AuthOperationService {

    @Autowired
    private UserService userService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private UserStatusRecordService userStatusRecordService;

    @Override
    @Transactional
    public RegisterResp register(RegisterReq registerReq) {
        Preconditions.checkArgument(registerReq != null, "参数错误");
        Preconditions.checkArgument(registerReq.getUsername() != null, "用户名不能为空");
        Preconditions.checkArgument(registerReq.getPassword() != null, "密码不能为空");
        Preconditions.checkArgument(registerReq.getUserRole() != null, "用户角色不能为空");

        User existUser = userService.queryByUsername(registerReq.getUsername());
        if (ObjectUtils.isNotEmpty(existUser)) {
            return new RegisterResp(BaseRespConstant.USER_EXIST);
        }

        String encryptedPwd = PasswordUtil.sha256(registerReq.getPassword());
        User user = new User();
        user.setUsername(registerReq.getUsername());
        user.setPhone(registerReq.getPhone());
        user.setPassword(encryptedPwd);
        user.setRole(registerReq.getUserRole());
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setCreateTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        Long upsert = userService.upsert(user);
        if (upsert <= 0) {
            return new RegisterResp(BaseRespConstant.FAIL);
        }
        user = userService.queryByUsername(user.getUsername());
        if (registerReq.getUserRole() == UserRole.NORMAL_USER.getCode()) {
            Wallet wallet = new Wallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(0L);
            wallet.setCreateTime(System.currentTimeMillis());
            wallet.setUpdateTime(System.currentTimeMillis());
            walletService.upsert(wallet);
        }
        return new RegisterResp(BaseRespConstant.SUC);
    }

    @Override
    public LogInResp login(LogInReq logInReq) {
        Preconditions.checkArgument(logInReq != null, "参数错误");
        Preconditions.checkArgument(logInReq.getUsername() != null, "用户名不能为空");
        Preconditions.checkArgument(logInReq.getPassword() != null, "密码不能为空");
        Preconditions.checkArgument(logInReq.getRole() != null, "用户角色不能为空");

        User user = userService.queryByUsername(logInReq.getUsername());
        if (ObjectUtils.isEmpty(user)) {
            return new LogInResp(BaseRespConstant.USER_NOT_EXIST);
        }
        if (StringUtils.equals(user.getUsername(), FrozenChefCleanupService.SYSTEM_USERNAME)) {
            return new LogInResp(new BaseResp(BaseRespConstant.FORBIDDEN.getCode(), "系统账号不允许登录"));
        }
        if (!logInReq.getRole().equals(user.getRole())) {
            return new LogInResp(BaseRespConstant.FORBIDDEN);
        }
        if (!Objects.equals(user.getStatus(), UserStatus.NORMAL.getCode())) {
            return new LogInResp(buildStatusErrorResp(user.getId()));
        }

        String encryptedPwd = PasswordUtil.sha256(logInReq.getPassword());
        if (!encryptedPwd.equals(user.getPassword())) {
            return new LogInResp(BaseRespConstant.PASSWORD_ERROR);
        }

        user.setLastLoginTime(System.currentTimeMillis());
        user.setUpdateTime(System.currentTimeMillis());
        userService.upsert(user);
        String token = JWTUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        RBucket<String> bucket = redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + user.getId());
        bucket.set(token, 1, TimeUnit.DAYS);
        return new LogInResp(BaseRespConstant.SUC, token, user.getUsername(), String.valueOf(user.getId()));
    }

    @Override
    public void logout(Long currentUserId, String authorizationHeader) {
        Long userId = currentUserId;
        if (userId == null || userId <= 0L) {
            Preconditions.checkArgument(StringUtils.isNotBlank(authorizationHeader), "token不能为空");
            String token = StringUtils.substringAfter(authorizationHeader, AuthConstant.BEARER_PREFIX);
            userId = JWTUtil.getUserId(token);
        }
        redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + userId).delete();
    }

    private BaseResp buildStatusErrorResp(Long userId) {
        String desc = BaseRespConstant.STATUS_ERROR.getDesc();
        if (userId == null) {
            return new BaseResp(BaseRespConstant.STATUS_ERROR.getCode(), desc);
        }
        UserStatusRecord frozenRecord = userStatusRecordService.queryByUserId(userId).stream()
                .filter(record -> record != null
                        && Objects.equals(record.getAfterStatus(), UserStatus.FROZEN.getCode())
                        && StringUtils.isNotBlank(record.getReason()))
                .findFirst()
                .orElse(null);
        if (frozenRecord != null) {
            desc = "账号已冻结，原因：" + frozenRecord.getReason();
        }
        return new BaseResp(BaseRespConstant.STATUS_ERROR.getCode(), desc);
    }
}

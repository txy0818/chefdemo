package com.txy.chefdemo.utils;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import org.apache.commons.lang3.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public final class AuthRequestUtils {

    private AuthRequestUtils() {
    }

    public static Long requireCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthConstant.REQUEST_USER_ID);
        Preconditions.checkArgument(userId instanceof Long && (Long) userId > 0L, "登录信息无效");
        return (Long) userId;
    }

    public static Integer requireCurrentUserRole(HttpServletRequest request) {
        Object userRole = request.getAttribute(AuthConstant.REQUEST_USER_ROLE);
        Preconditions.checkArgument(userRole instanceof Integer && (Integer) userRole > 0, "登录信息无效");
        return (Integer) userRole;
    }

    public static Long requireUser(HttpServletRequest request, UserRole role) {
        Long userId = requireCurrentUserId(request);
        Integer userRole = requireCurrentUserRole(request);
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(userRole) && Objects.equals(userRole, role.getCode()), BaseRespConstant.FORBIDDEN.getDesc());
        return userId;
    }
}

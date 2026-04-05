package com.txy.chefdemo.interceptor;

import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.resp.BaseResp;
import com.txy.chefdemo.resp.SimpleResp;
import com.txy.chefdemo.utils.ObjectMapperUtils;
import com.txy.chefdemo.utils.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS
        // 浏览器的预检请求，不做登录校验，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String authorization = request.getHeader(AuthConstant.AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(authorization) || !StringUtils.startsWith(authorization, AuthConstant.BEARER_PREFIX)) {
            writeAuthFail(response, "缺少有效token");
            return false;
        }

        String token = StringUtils.substringAfter(authorization, AuthConstant.BEARER_PREFIX);
        if (!JWTUtil.validateToken(token)) {
            writeAuthFail(response, "token无效");
            return false;
        }

        Long userId = JWTUtil.getUserId(token);
        Integer userRole = JWTUtil.getUserRole(token);
        RBucket<String> bucket = redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + userId);
        String cachedToken = bucket.get();
        if (!StringUtils.equals(token, cachedToken)) {
            writeAuthFail(response, "登录状态已失效");
            return false;
        }
        if (!hasPathPermission(request.getRequestURI(), userRole)) {
            writeAuthFail(response, "无权限");
            return false;
        }

        request.setAttribute(AuthConstant.REQUEST_USER_ID, userId);
        request.setAttribute(AuthConstant.REQUEST_USER_ROLE, userRole);
        return true;
    }

    private boolean hasPathPermission(String requestUri, Integer userRole) {
        if (StringUtils.isBlank(requestUri) || userRole == null) {
            return false;
        }
        if (requestUri.startsWith("/admin/")) {
            return userRole.equals(UserRole.ADMIN.getCode());
        }
        if (requestUri.startsWith("/chef/")) {
            return userRole.equals(UserRole.CHEF.getCode());
        }
        if (requestUri.startsWith("/user/")) {
            return userRole.equals(UserRole.NORMAL_USER.getCode());
        }
        return true;
    }

    private void writeAuthFail(HttpServletResponse response, String desc) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(ObjectMapperUtils.toJSON(new SimpleResp(new BaseResp(11, desc))));
    }
}

package com.txy.chefdemo.config;

import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.utils.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 暴露 websocket 端点
// 例：ws://localhost:8080/ws/123?token=xxxxx
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    private static final Map<Long, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        if (!isAuthorized(session, userId)) {
            closeQuietly(session);
            return;
        }
        Session oldSession = SESSION_MAP.put(userId, session);
        if (oldSession != null && oldSession.isOpen() && oldSession != session) {
            closeQuietly(oldSession);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        Session currentSession = SESSION_MAP.get(userId);
        if (currentSession == session) {
            SESSION_MAP.remove(userId);
        }
    }

    public static void send(Long userId, String msg) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(msg);
        }
    }

    private boolean isAuthorized(Session session, Long userId) {
        if (session == null || userId == null) {
            return false;
        }
        List<String> tokens = session.getRequestParameterMap().get("token");
        String token = tokens == null || tokens.isEmpty() ? null : tokens.get(0);
        if (!JWTUtil.validateToken(token)) {
            return false;
        }
        Long tokenUserId = JWTUtil.getUserId(token);
        if (!userId.equals(tokenUserId)) {
            return false;
        }
        RedissonClient redissonClient = SpringContextHolder.getBean(RedissonClient.class);
        if (redissonClient == null) {
            return false;
        }
        RBucket<String> bucket = redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + tokenUserId);
        String cachedToken = bucket.get();
        return StringUtils.equals(token, cachedToken);
    }

    private void closeQuietly(Session session) {
        if (session == null) {
            return;
        }
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}

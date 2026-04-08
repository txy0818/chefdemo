package com.txy.chefdemo.config;

import com.txy.chefdemo.constant.AuthConstant;
import com.txy.chefdemo.utils.JWTUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.CollectionUtils;

/**
 * WebSocket 服务端端点。
 *
 * 调用时机：
 * 1. 前端执行 new WebSocket("ws://.../ws/{userId}?token=xxx") 发起握手时，容器会自动回调 onOpen
 * 2. 浏览器主动关闭连接socket.close()、页面刷新/离开、网络断开、服务端调用 session.close() 时，容器会自动回调 onClose
 * 3. 业务代码需要主动给某个用户推消息时，调用send
 *
 * 例：
 * ws://localhost:8080/ws/123?token=xxxxx
 */
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    private static final Map<Long, Session> SESSION_MAP = new ConcurrentHashMap<>();
    @Autowired
    private RedissonClient redissonClient;

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        // 新连接建立后立刻鉴权；鉴权失败则马上关闭，不放入连接池。
        if (!isAuthorized(session, userId)) {
            closeQuietly(session);
            return;
        }
        // 一个 userId 只保留一个最新连接。
        // 如果同一用户重复建立连接（比如刷新页面），就用新连接覆盖旧连接，并关闭旧连接。
        // Map.put(key, value) 的返回值是：该 key 之前对应的旧值（old value）
        Session oldSession = SESSION_MAP.put(userId, session);
        if (Objects.nonNull(oldSession) && oldSession.isOpen() && oldSession != session) {
            closeQuietly(oldSession);
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        // 连接关闭时清理 SESSION_MAP。
        // 这里只移除“当前仍登记的那条连接”，避免旧连接关闭时把新连接误删掉。
        Session currentSession = SESSION_MAP.get(userId);
        if (currentSession == session) {
            SESSION_MAP.remove(userId);
        }
    }

    public static void send(Long userId, String msg) {
        // 业务层按 userId 推送消息时，从连接池取出该用户当前在线连接发送。
        Session session = SESSION_MAP.get(userId);
        if (Objects.nonNull(session) && session.isOpen()) {
            session.getAsyncRemote().sendText(msg);
        }
    }

    private boolean isAuthorized(Session session, Long userId) {
        if (Objects.isNull(session) || Objects.isNull(userId)) {
            return false;
        }
        // 读取的是URL 上的 query 参数
        // Session#getRequestParameterMap() 拿到的就是请求参数。
        List<String> tokens = session.getRequestParameterMap().get("token");
        String token = CollectionUtils.isEmpty(tokens) ? null : tokens.get(0);
        // 先校验 token 本身是否合法（签名、过期时间等）。
        if (!JWTUtil.validateToken(token)) {
            return false;
        }
        // 再校验 URL 里的 userId 是否和 token 内声明的 userId 一致，防止拿着别人的 userId 建连。
        Long tokenUserId = JWTUtil.getUserId(token);
        if (!userId.equals(tokenUserId)) {
            return false;
        }
        if (Objects.isNull(redissonClient)) {
            return false;
        }
        RBucket<String> bucket = redissonClient.getBucket(AuthConstant.LOGIN_TOKEN_KEY_PREFIX + tokenUserId);
        String cachedToken = bucket.get();
        return StringUtils.equals(token, cachedToken);
    }

    private void closeQuietly(Session session) {
        if (Objects.isNull(session)) {
            return;
        }
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}

package com.txy.chefdemo.service;

import com.txy.chefdemo.config.WebSocketServer;
import com.txy.chefdemo.domain.NotificationRecord;
import com.txy.chefdemo.domain.constant.WebSocketMessageType;
import com.txy.chefdemo.domain.dto.WebSocketMessageDTO;
import com.txy.chefdemo.utils.ObjectMapperUtils;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void notifyUser(NotificationRecord record) {
        notifyUser(record, WebSocketMessageType.NOTIFICATION);
    }

    public void notifyUser(NotificationRecord record, String messageType) {
        WebSocketMessageDTO message = new WebSocketMessageDTO(
                messageType,
                record.getTitle(),
                record.getContent(),
                record.getBizId(),
                record
        );
        WebSocketServer.send(record.getUserId(), ObjectMapperUtils.toJSON(message));
    }

    public void forceLogout(Long userId, String title, String content) {
        WebSocketMessageDTO message = new WebSocketMessageDTO(
                WebSocketMessageType.FORCE_LOGOUT,
                title,
                content,
                userId,
                null
        );
        WebSocketServer.send(userId, ObjectMapperUtils.toJSON(message));
    }
}

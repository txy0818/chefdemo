package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {
    private String type;
    private String title;
    private String content;
    private Long bizId;
    private Object data;
}

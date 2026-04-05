package com.txy.chefdemo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusChangeEvent {
    private Long userId;
    private Integer status;
    private Long operatorId;
}
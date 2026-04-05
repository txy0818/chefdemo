package com.txy.chefdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUploadDTO {
    private String objectKey;
    private String uploadUrl;
    private String fileUrl;
    private Integer expireSeconds;
}

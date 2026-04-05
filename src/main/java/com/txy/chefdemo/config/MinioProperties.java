package com.txy.chefdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /** MinIO 服务地址，例如 http://127.0.0.1:9000 */
    private String endpoint;
    /** 对外访问地址，未配置时默认使用 endpoint */
    private String publicEndpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    /** presigned url 过期时间，单位秒 */
    private Integer uploadExpirySeconds = 900;
    /** 启动时自动创建 bucket 并设置公开读 */
    private Boolean autoCreateBucket = true;
}

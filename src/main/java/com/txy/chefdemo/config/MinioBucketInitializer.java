package com.txy.chefdemo.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MinioBucketInitializer {

    // 后端签名 + 前端直传
    // 调用chef-frontend/setup-minio.sh，通过mc将minio的跨域等设置

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioBucketInitializer(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void initBucket() {
        if (!Boolean.TRUE.equals(minioProperties.getAutoCreateBucket())) {
            return;
        }
        try {
            String bucket = minioProperties.getBucket();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(buildPublicReadPolicy(bucket))
                    .build());
            log.info("MinIO bucket policy set to public-read: {}", bucket);
            if (StringUtils.isBlank(minioProperties.getPublicEndpoint())) {
                minioProperties.setPublicEndpoint(minioProperties.getEndpoint());
            }
        } catch (Exception e) {
            log.error("MinIO bucket init failed", e);
        }
    }

    /** 公开下载（只读），上传/删除需要密钥（私有写）*/
    private String buildPublicReadPolicy(String bucket) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                "      \"Action\": [\"s3:GetObject\"],\n" +
                "      \"Resource\": [\"arn:aws:s3:::" + bucket + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}

package com.txy.chefdemo.controller;

import com.google.common.base.Preconditions;
import com.txy.chefdemo.aspect.LogExecution;
import com.txy.chefdemo.config.MinioProperties;
import com.txy.chefdemo.domain.dto.PresignedUploadDTO;
import com.txy.chefdemo.req.PresignedUploadReq;
import com.txy.chefdemo.resp.DataResp;
import com.txy.chefdemo.resp.constants.BaseRespConstant;
import com.txy.chefdemo.utils.AuthRequestUtils;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "images",
            "avatars",
            "chef-images",
            "certificates",
            "dishes",
            "temp"
    );

    /**
     * 这个 controller 只做一件事：
     * 不让前端直接拿 MinIO 的 accessKey/secretKey，
     * 而是由后端生成一个临时可用的 presigned upload url 给前端。
     *
     * 整体流程：
     * 1. 前端先把 folder/fileName/contentType 发给后端
     * 2. 后端根据当前登录用户 + 日期 + 随机串，拼出 objectKey
     * 3. 后端调用 MinIO SDK 生成一个临时 PUT 上传地址 uploadUrl
     * 4. 前端再拿这个 uploadUrl 直接把文件 PUT 到 MinIO
     * 5. 上传成功后，前端使用 fileUrl 作为最终文件访问地址
     */
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public FileController(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    /**
     * 生成 MinIO 预签名上传地址：
     * 1. 校验文件名；
     * 2. 从鉴权上下文中取当前登录用户 ID；
     * 3. 清洗业务目录并生成最终 objectKey；
     * 4. 用 MinIO SDK 生成临时 PUT 上传地址；
     * 5. 返回 uploadUrl 和最终可访问的 fileUrl 给前端。
     */
    @LogExecution(returnType = DataResp.class)
    @PostMapping("/presigned/upload")
    public DataResp<PresignedUploadDTO> getPresignedUploadUrl(@RequestBody PresignedUploadReq req,
                                                              HttpServletRequest request) throws Exception {
        Preconditions.checkArgument(ObjectUtils.isNotEmpty(req), "参数错误");
        Preconditions.checkArgument(StringUtils.isNotBlank(req.getFileName()), "文件名不能为空");

        // 这个 userId 不是前端传的，而是登录鉴权通过后，拦截器放进 request 里的当前登录用户 id。
        Long userId = AuthRequestUtils.requireCurrentUserId(request);

        // folder 只允许保留安全字符，避免传入 ../../ 之类的非法路径。
        String folder = sanitizeFolder(req.getFolder());
        // 生成 MinIO 中真正存储的对象路径，例如 avatars/3/20260404/uuid.jpeg
        String objectKey = buildObjectKey(folder, userId, req.getFileName());
        int expiry = ObjectUtils.defaultIfNull(minioProperties.getUploadExpirySeconds(), 900);

        // 生成一个临时有效的 PUT 地址，前端拿这个地址直接上传二进制文件。
        String uploadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(minioProperties.getBucket())
                        .object(objectKey)
                        .expiry(expiry, TimeUnit.SECONDS)
                        .build()
        );

        // fileUrl 是上传成功后，前端/数据库里真正保存和展示的文件访问地址。
        String fileUrl = buildFileUrl(objectKey);
        return new DataResp<>(BaseRespConstant.SUC,
                new PresignedUploadDTO(objectKey, uploadUrl, fileUrl, expiry));
    }

    /**
     * 对业务目录做一层清洗：
     * - 默认目录是 images
     * - 统一成 /
     * - 去掉除字母、数字、下划线、横杠、斜杠以外的字符
     */
    private String sanitizeFolder(String folder) {
        String sanitized = StringUtils.defaultIfBlank(folder, "images")
                .replace("\\", "/")
                .replaceAll("[^a-zA-Z0-9/_-]", "");
        sanitized = sanitized.replaceAll("/+", "/");
        sanitized = StringUtils.strip(sanitized, "/");
        sanitized = StringUtils.isBlank(sanitized) ? "images" : sanitized;
        Preconditions.checkArgument(ALLOWED_FOLDERS.contains(sanitized), "非法上传目录");
        return sanitized;
    }

    /**
     * 生成对象 key。
     *
     * 为什么不直接用原始文件名？
     * - 原始文件名可能重复
     * - 原始文件名可能有中文/空格/特殊字符
     * - 用 userId + 日期 + uuid 更稳定，也方便按用户分目录管理
     *
     * 最终形式：
     * folder/userId/yyyyMMdd/uuid.ext
     */
    private String buildObjectKey(String folder, Long userId, String fileName) {
        String ext = "";
        String safeName = StringUtils.defaultIfBlank(fileName, "file");
        int idx = safeName.lastIndexOf('.');
        if (idx >= 0 && idx < safeName.length() - 1) {
            ext = safeName.substring(idx).toLowerCase();
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return folder + "/" + userId + "/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    /**
     * 拼接最终访问地址。
     * 这里不是签名地址，而是公开访问地址。
     */
    private String buildFileUrl(String objectKey) {
        String publicEndpoint = StringUtils.defaultIfBlank(minioProperties.getPublicEndpoint(), minioProperties.getEndpoint());
        return StringUtils.removeEnd(publicEndpoint, "/") + "/" + minioProperties.getBucket() + "/" + objectKey;
    }
}

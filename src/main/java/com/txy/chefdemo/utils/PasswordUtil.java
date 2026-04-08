package com.txy.chefdemo.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String sha256(String input) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不存在", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 整数转成十六进制
            // byte：0-255（每个字节都固定输出成两位十六进制） 10 -> a (0a)
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase();
    }

}

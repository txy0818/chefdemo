package com.txy.chefdemo.utils;

import io.jsonwebtoken.*;
import java.util.Date;

/**
 * @Author tianxinyu
 * @Create 2026-03-03
 */
public class JWTUtil {

    private static final String SECRET_KEY = "MySuperSuperSecretKeyThatIsLongEnoughForHS256123!";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000; // 1天过期

    /**
     * 生成 JWT Token
     *
     * .setSubject("123") "sub": "123"  JWT标准字段，主题
     * .setIssuedAt(new Date()) "iat": 1234567890  JWT标准字段，签发时间
     * .setExpiration(...) "exp": 1234567890  JWT标准字段，过期时间
     * .claim("username", username) "username": "张三" 自定义字段
     * .claim("role", role) "role": 3 自定义字段
     *
     * "iat": 1710000000 token 生成时间
     */
    public static String generateToken(Long userId, String username, Integer role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    /**
     * 解析 JWT Token
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token 已过期", e);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new RuntimeException("Token 无效", e);
        }
    }

    /**
     * 校验 Token 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 可以加额外逻辑，比如验证用户ID或角色
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取用户ID（subject）
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 获取用户角色
     */
    public static Integer getUserRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", Integer.class);
    }

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("JWT Token 生成器和测试工具");
        System.out.println("=".repeat(80));
        System.out.println();

        // 测试用户数据
        // eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiIsInJvbGUiOjEsImlhdCI6MTc3NTE0MDYyNywiZXhwIjoxNzc1MjI3MDI3fQ.FFJxs1uEXAeS-1gSMLOQ93nA0cVasOfaUWNkQCi2Vmc
        Long adminUserId = 1L;
        String adminUsername = "admin";
        Integer adminRole = 1; // 1-管理员

        Long chefUserId = 2L;
        String chefUsername = "chef001";
        Integer chefRole = 2; // 2-厨师

        Long normalUserId = 3L;
        String normalUsername = "user001";
        Integer normalRole = 3; // 3-普通用户

        // 生成不同角色的Token
        System.out.println("1. 生成不同角色的JWT Token:");
        System.out.println("-".repeat(80));
        
        String adminToken = generateToken(adminUserId, adminUsername, adminRole);
        System.out.println("管理员Token:");
        System.out.println("  用户ID: " + adminUserId);
        System.out.println("  用户名: " + adminUsername);
        System.out.println("  角色: " + adminRole + " (管理员)");
        System.out.println("  Token: " + adminToken);
        System.out.println();

        String chefToken = generateToken(chefUserId, chefUsername, chefRole);
        System.out.println("厨师Token:");
        System.out.println("  用户ID: " + chefUserId);
        System.out.println("  用户名: " + chefUsername);
        System.out.println("  角色: " + chefRole + " (厨师)");
        System.out.println("  Token: " + chefToken);
        System.out.println();

        String normalToken = generateToken(normalUserId, normalUsername, normalRole);
        System.out.println("普通用户Token:");
        System.out.println("  用户ID: " + normalUserId);
        System.out.println("  用户名: " + normalUsername);
        System.out.println("  角色: " + normalRole + " (普通用户)");
        System.out.println("  Token: " + normalToken);
        System.out.println();
    }
}
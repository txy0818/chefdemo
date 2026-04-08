package com.txy.chefdemo.utils;

import com.txy.chefdemo.domain.constant.UserRole;
import com.txy.chefdemo.domain.constant.UserStatus;
import com.txy.chefdemo.service.FrozenChefCleanupService;

/**
 * 初始化用户 SQL 生成器：
 * 1. 生成管理员 admin/123 的插入 SQL；
 * 2. 生成系统账号 system 的插入 SQL；
 * 3. 便于本地快速初始化默认账号。
 */
public class InitUserSqlGenerator {

    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        System.out.println(generateAdminInsertSql(now));
        System.out.println();
        System.out.println(generateSystemInsertSql(now));
    }

    public static String generateAdminInsertSql(long now) {
        return buildInsertSql("admin", "123", UserRole.ADMIN.getCode(), now);
    }

    public static String generateSystemInsertSql(long now) {
        return buildInsertSql(FrozenChefCleanupService.SYSTEM_USERNAME, "system", UserRole.ADMIN.getCode(), UserStatus.FROZEN.getCode(), now);
    }

    private static String buildInsertSql(String username, String password, int role, long now) {
        return buildInsertSql(username, password, role, UserStatus.NORMAL.getCode(), now);
    }

    private static String buildInsertSql(String username, String password, int role, int status, long now) {
        return String.format(
                "INSERT INTO user (username, password, role, avatar, phone, status, last_login_time, create_time, update_time) "
                        + "VALUES ('%s', '%s', %d, '', '', %d, %d, %d, %d);",
                username,
                PasswordUtil.sha256(password),
                role,
                status,
                now,
                now,
                now
        );
    }
}

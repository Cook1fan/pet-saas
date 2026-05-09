package com.pet.saas;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordGenerator {
    public static void main(String[] args) {
        String plainPassword = "admin123";
        String hashedPassword = BCrypt.hashpw(plainPassword);

        System.out.println("明文密码: " + plainPassword);
        System.out.println("BCrypt 哈希: " + hashedPassword);
        System.out.println();
        System.out.println("请执行以下 SQL 更新数据库:");
        System.out.println("UPDATE sys_platform_admin SET password = '" + hashedPassword + "' WHERE username = 'admin';");

        // 验证一下
        boolean matches = BCrypt.checkpw(plainPassword, hashedPassword);
        System.out.println();
        System.out.println("验证结果: " + (matches ? "匹配成功" : "匹配失败"));
    }
}

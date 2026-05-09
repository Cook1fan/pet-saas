package com.pet.saas.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 拼团组号生成工具类
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
public class GroupNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    public static String generateGroupNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int random = RANDOM.nextInt(900000) + 100000; // 6位随机数
        return "GRP" + timestamp + random;
    }
}

package com.pet.saas.common.util;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDateTime;

public class VerifyCodeUtil {

    /**
     * 核销码长度
     */
    private static final int CODE_LENGTH = 6;

    /**
     * 核销码有效期（分钟）
     */
    public static final int EXPIRATION_MINUTES = 5;

    /**
     * 生成6位数字核销码
     *
     * @return 核销码
     */
    public static String generateCode() {
        return RandomUtil.randomNumbers(CODE_LENGTH);
    }

    /**
     * 计算过期时间
     *
     * @return 过期时间
     */
    public static LocalDateTime calculateExpireTime() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    /**
     * 检查核销码是否过期
     *
     * @param expireTime 过期时间
     * @return 是否过期
     */
    public static boolean isExpired(LocalDateTime expireTime) {
        return LocalDateTime.now().isAfter(expireTime);
    }
}

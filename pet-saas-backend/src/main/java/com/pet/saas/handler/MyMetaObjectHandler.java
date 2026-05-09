package com.pet.saas.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("start insert fill ....");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createUser", Long.class, userId);
            this.strictInsertFill(metaObject, "updateUser", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("start update fill ....");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateUser", Long.class, userId);
        }
    }

    private Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                Object loginId = StpUtil.getLoginIdDefaultNull();
                if (loginId != null) {
                    return extractNumericId(loginId.toString());
                }
            }
        } catch (Exception e) {
            log.debug("获取当前登录用户失败，可能未登录: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从带前缀的 loginId 中提取数值 ID
     * 格式: {prefix}:{id}，例如 "platform:1"、"shop:1"、"member:1"
     */
    private Long extractNumericId(String loginId) {
        if (loginId == null) {
            return null;
        }
        int colonIndex = loginId.indexOf(':');
        if (colonIndex != -1 && colonIndex < loginId.length() - 1) {
            String numericPart = loginId.substring(colonIndex + 1);
            try {
                return Long.valueOf(numericPart);
            } catch (NumberFormatException e) {
                log.debug("解析 loginId 失败: {}", loginId);
            }
        }
        // 兼容旧格式，直接尝试转换
        try {
            return Long.valueOf(loginId);
        } catch (NumberFormatException e) {
            log.debug("转换 loginId 为 Long 失败: {}", loginId);
            return null;
        }
    }
}

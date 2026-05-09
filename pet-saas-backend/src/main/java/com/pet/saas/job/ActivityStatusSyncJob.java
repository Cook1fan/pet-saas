package com.pet.saas.job;

import com.pet.saas.entity.ActivityInfo;
import com.pet.saas.mapper.ActivityInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动状态同步定时任务
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusSyncJob {

    private final ActivityInfoMapper activityInfoMapper;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "30 * * * * ?")
    public void syncActivityStatus() {
        log.info("开始执行活动状态同步任务");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. 检查应该开始但未开始的活动
            List<ActivityInfo> toStart = activityInfoMapper.selectList(
                    new LambdaQueryWrapper<ActivityInfo>()
                            .eq(ActivityInfo::getStatus, 0) // 未开始
                            .le(ActivityInfo::getStartTime, now)
            );
            for (ActivityInfo activity : toStart) {
                activity.setStatus(1);
                activityInfoMapper.updateById(activity);
                log.info("活动已开始，activityId={}", activity.getId());
            }

            // 2. 检查应该结束但未结束的活动
            List<ActivityInfo> toEnd = activityInfoMapper.selectList(
                    new LambdaQueryWrapper<ActivityInfo>()
                            .eq(ActivityInfo::getStatus, 1) // 进行中
                            .le(ActivityInfo::getEndTime, now)
            );
            for (ActivityInfo activity : toEnd) {
                activity.setStatus(2);
                activityInfoMapper.updateById(activity);
                log.info("活动已结束，activityId={}", activity.getId());
            }

        } catch (Exception e) {
            log.error("活动状态同步任务异常", e);
        }

        log.info("活动状态同步任务执行完成");
    }
}

package com.pet.saas.job;

import com.pet.saas.common.enums.ActivityGroupStatusEnum;
import com.pet.saas.entity.ActivityGroup;
import com.pet.saas.mapper.ActivityGroupMapper;
import com.pet.saas.service.GroupActivityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团过期处理定时任务
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupExpireJob {

    private final ActivityGroupMapper activityGroupMapper;
    private final GroupActivityService groupActivityService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processExpiredGroups() {
        log.info("开始执行拼团过期处理任务");

        try {
            // 查询所有过期未成团的拼团组
            List<ActivityGroup> groups = activityGroupMapper.selectList(
                    new LambdaQueryWrapper<ActivityGroup>()
                            .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.GROUPING.getCode())
                            .le(ActivityGroup::getExpireTime, LocalDateTime.now())
            );

            log.info("发现{}个过期拼团组", groups.size());

            for (ActivityGroup group : groups) {
                try {
                    groupActivityService.handleGroupFail(group.getId());
                    log.info("处理拼团失败完成，groupId={}", group.getId());
                } catch (Exception e) {
                    log.error("处理拼团失败异常，groupId={}", group.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("拼团过期处理任务异常", e);
        }

        log.info("拼团过期处理任务执行完成");
    }
}

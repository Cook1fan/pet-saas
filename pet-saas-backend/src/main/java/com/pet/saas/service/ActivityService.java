package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.controller.pc.ActivityController;
import com.pet.saas.dto.query.ActivityQuery;
import com.pet.saas.entity.ActivityInfo;
import com.pet.saas.entity.ActivityOrder;

import java.util.Map;

public interface ActivityService {

    ActivityInfo createGroupActivity(ActivityController.CreateGroupActivityReq req, Long tenantId);

    ActivityInfo createSeckillActivity(ActivityController.CreateSeckillActivityReq req, Long tenantId);

    ActivityInfo updateGroupActivity(Long activityId, ActivityController.CreateGroupActivityReq req, Long tenantId);

    ActivityInfo updateSeckillActivity(Long activityId, ActivityController.CreateSeckillActivityReq req, Long tenantId);

    void deleteActivity(Long activityId, Long tenantId);

    void updateActivityStatus(Long activityId, Integer status, Long tenantId);

    Page<ActivityInfo> listActivities(ActivityQuery query, Long tenantId);

    ActivityInfo getActivity(Long activityId);

    ActivityOrder createActivityOrder(Long tenantId, Long activityId, Long memberId, Long orderId);

    Map<String, Object> getActivityData(Long activityId);
}

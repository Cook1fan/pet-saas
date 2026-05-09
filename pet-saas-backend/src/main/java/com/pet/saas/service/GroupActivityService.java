package com.pet.saas.service;

import com.pet.saas.dto.query.ActivityGroupQuery;
import com.pet.saas.dto.query.GroupActivityQuery;
import com.pet.saas.dto.query.MyGroupOrderQuery;
import com.pet.saas.dto.req.JoinGroupReq;
import com.pet.saas.dto.req.LaunchGroupReq;
import com.pet.saas.dto.req.CreateGroupActivityReq;
import com.pet.saas.dto.resp.*;

/**
 * 拼团活动服务接口
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
public interface GroupActivityService {

    // ========== 商家端 ==========

    /** 创建拼团活动 */
    CreateGroupActivityResp createActivity(CreateGroupActivityReq req, Long tenantId, Long adminId);

    /** 拼团活动列表 */
    PageResult<GroupActivityListResp> getActivityList(GroupActivityQuery query, Long tenantId);

    /** 拼团活动详情 */
    GroupActivityDetailResp getActivityDetail(Long activityId, Long tenantId);

    /** 拼团组列表 */
    PageResult<ActivityGroupListResp> getGroupList(Long activityId, ActivityGroupQuery query, Long tenantId);

    // ========== C端 ==========

    /** 拼团活动列表 */
    java.util.List<UserGroupActivityListResp> getUserActivityList(Long tenantId);

    /** 拼团活动详情 */
    UserGroupActivityDetailResp getUserActivityDetail(Long activityId, Long tenantId, Long memberId);

    /** 拼团组详情 */
    UserGroupDetailResp getGroupDetail(Long groupId, Long tenantId, Long memberId);

    /** 发起拼团 */
    CreateGroupOrderResp launchGroup(LaunchGroupReq req, Long tenantId, Long memberId);

    /** 加入拼团 */
    CreateGroupOrderResp joinGroup(JoinGroupReq req, Long tenantId, Long memberId);

    /** 我的拼团订单列表 */
    PageResult<MyGroupOrderResp> getMyOrderList(MyGroupOrderQuery query, Long tenantId, Long memberId);

    // ========== 回调处理 ==========

    /** 支付成功处理 */
    void handlePaySuccess(Long orderId);

    /** 拼团失败处理 */
    void handleGroupFail(Long groupId);
}

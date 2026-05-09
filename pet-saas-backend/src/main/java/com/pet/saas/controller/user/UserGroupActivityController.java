package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.MyGroupOrderQuery;
import com.pet.saas.dto.req.JoinGroupReq;
import com.pet.saas.dto.req.LaunchGroupReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.service.GroupActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-拼团活动")
@RestController
@RequestMapping("/api/user/activity/group")
@RequiredArgsConstructor
public class UserGroupActivityController {

    private final GroupActivityService groupActivityService;

    @Operation(summary = "拼团活动列表")
    @GetMapping("/list")
    public R<List<UserGroupActivityListResp>> getGroupActivityList() {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        List<UserGroupActivityListResp> list = groupActivityService.getUserActivityList(tenantId);
        return R.ok(list);
    }

    @Operation(summary = "拼团活动详情")
    @GetMapping("/detail/{activityId}")
    public R<UserGroupActivityDetailResp> getGroupActivityDetail(
            @Parameter(description = "活动ID", required = true)
            @PathVariable @NotNull Long activityId) {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = StpKit.MEMBER.getLoginIdAsLong();
        UserGroupActivityDetailResp resp = groupActivityService.getUserActivityDetail(activityId, tenantId, memberId);
        return R.ok(resp);
    }

    @Operation(summary = "拼团组详情")
    @GetMapping("/group-detail/{groupId}")
    public R<UserGroupDetailResp> getGroupDetail(
            @Parameter(description = "拼团组ID", required = true)
            @PathVariable @NotNull Long groupId) {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = StpKit.MEMBER.getLoginIdAsLong();
        UserGroupDetailResp resp = groupActivityService.getGroupDetail(groupId, tenantId, memberId);
        return R.ok(resp);
    }

    @Operation(summary = "发起拼团")
    @PostMapping("/launch")
    public R<CreateGroupOrderResp> launchGroup(@Valid @RequestBody LaunchGroupReq req) {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = StpKit.MEMBER.getLoginIdAsLong();
        CreateGroupOrderResp resp = groupActivityService.launchGroup(req, tenantId, memberId);
        return R.ok(resp);
    }

    @Operation(summary = "加入拼团")
    @PostMapping("/join")
    public R<CreateGroupOrderResp> joinGroup(@Valid @RequestBody JoinGroupReq req) {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = StpKit.MEMBER.getLoginIdAsLong();
        CreateGroupOrderResp resp = groupActivityService.joinGroup(req, tenantId, memberId);
        return R.ok(resp);
    }

    @Operation(summary = "我的拼团订单列表")
    @GetMapping("/my-orders")
    public R<PageResult<MyGroupOrderResp>> getMyOrderList(
            @ParameterObject @Valid MyGroupOrderQuery query) {
        Long tenantId = (Long) StpKit.MEMBER.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = StpKit.MEMBER.getLoginIdAsLong();
        PageResult<MyGroupOrderResp> page = groupActivityService.getMyOrderList(query, tenantId, memberId);
        return R.ok(page);
    }
}

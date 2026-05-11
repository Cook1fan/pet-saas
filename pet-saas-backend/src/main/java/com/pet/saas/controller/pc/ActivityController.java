package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.ActivityQuery;
import com.pet.saas.dto.query.ActivityGroupQuery;
import com.pet.saas.dto.resp.ActivityInfoVO;
import com.pet.saas.dto.resp.ActivityGroupListResp;
import com.pet.saas.dto.resp.PageResult;
import com.pet.saas.entity.ActivityInfo;
import com.pet.saas.service.ActivityService;
import com.pet.saas.service.GroupActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "门店PC端-营销活动")
@RestController
@RequestMapping("/api/pc/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final GroupActivityService groupActivityService;

    @Operation(summary = "活动列表")
    @GetMapping("/list")
    public R<PageResult<ActivityInfoVO>> listActivities(@ParameterObject @Valid ActivityQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<ActivityInfoVO> page = activityService.listActivities(query, tenantId);
        return R.ok(BeanConverter.convertToPageResult(page, ActivityInfoVO.class));
    }

    @Operation(summary = "创建拼团活动")
    @PostMapping("/group/create")
    public R<ActivityInfoVO> createGroupActivity(@Valid @RequestBody CreateGroupActivityReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        ActivityInfo activity = activityService.createGroupActivity(req, tenantId);
        return R.ok(activityService.getActivity(activity.getId()));
    }

    @Operation(summary = "创建秒杀活动")
    @PostMapping("/seckill/create")
    public R<ActivityInfoVO> createSeckillActivity(@Valid @RequestBody CreateSeckillActivityReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        ActivityInfo activity = activityService.createSeckillActivity(req, tenantId);
        return R.ok(activityService.getActivity(activity.getId()));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/{activityId}")
    public R<ActivityInfoVO> getActivityDetail(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId) {
        ActivityInfoVO activity = activityService.getActivity(activityId);
        return R.ok(activity);
    }

    @Operation(summary = "编辑拼团活动")
    @PutMapping("/group/update/{activityId}")
    public R<ActivityInfoVO> updateGroupActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId,
            @Valid @RequestBody CreateGroupActivityReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        activityService.updateGroupActivity(activityId, req, tenantId);
        return R.ok(activityService.getActivity(activityId));
    }

    @Operation(summary = "编辑秒杀活动")
    @PutMapping("/seckill/update/{activityId}")
    public R<ActivityInfoVO> updateSeckillActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId,
            @Valid @RequestBody CreateSeckillActivityReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        activityService.updateSeckillActivity(activityId, req, tenantId);
        return R.ok(activityService.getActivity(activityId));
    }

    @Operation(summary = "删除活动")
    @DeleteMapping("/{activityId}")
    public R<Void> deleteActivity(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        activityService.deleteActivity(activityId, tenantId);
        return R.ok();
    }

    @Operation(summary = "活动上架/下架")
    @PutMapping("/{activityId}/status")
    public R<Void> updateActivityStatus(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId,
            @Parameter(description = "状态 0-下架 1-上架", required = true) @RequestParam @NotNull Integer status) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        activityService.updateActivityStatus(activityId, status, tenantId);
        return R.ok();
    }

    @Operation(summary = "活动数据")
    @GetMapping("/data/{activityId}")
    public R<Map<String, Object>> getActivityData(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId) {
        Map<String, Object> data = activityService.getActivityData(activityId);
        return R.ok(data);
    }

    @Operation(summary = "拼团活动的团列表")
    @GetMapping("/group/detail/{activityId}/groups")
    public R<PageResult<ActivityGroupListResp>> getGroupList(
            @Parameter(description = "活动ID", required = true) @PathVariable @NotNull Long activityId,
            @ParameterObject @Valid ActivityGroupQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        PageResult<ActivityGroupListResp> page = groupActivityService.getGroupList(activityId, query, tenantId);
        return R.ok(page);
    }

    @Data
    @Schema(description = "创建拼团活动请求")
    public static class CreateGroupActivityReq {
        @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动标题不能为空")
        private String title;

        @Schema(description = "商品/服务ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "商品ID不能为空")
        private Long goodsId;

        @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        @Schema(description = "活动价格", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动价格不能为空")
        private BigDecimal price;

        @Schema(description = "原价", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "原价不能为空")
        private BigDecimal originPrice;

        @Schema(description = "成团人数", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "成团人数不能为空")
        private Integer groupCount;

        @Schema(description = "拼团有效期（小时）", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "拼团有效期不能为空")
        private Integer groupValidHours;

        @Schema(description = "活动库存", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动库存不能为空")
        private Integer stock;

        @Schema(description = "限购数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "限购数量不能为空")
        private Integer limitNum;

        @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "开始时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "结束时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }

    @Data
    @Schema(description = "创建秒杀活动请求")
    public static class CreateSeckillActivityReq {
        @Schema(description = "活动标题", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动标题不能为空")
        private String title;

        @Schema(description = "商品/服务ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "商品ID不能为空")
        private Long goodsId;

        @Schema(description = "活动价格", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动价格不能为空")
        private BigDecimal price;

        @Schema(description = "原价", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "原价不能为空")
        private BigDecimal originPrice;

        @Schema(description = "活动库存", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "活动库存不能为空")
        private Integer stock;

        @Schema(description = "限购数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "限购数量不能为空")
        private Integer limitNum;

        @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "开始时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "结束时间不能为空")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
    }
}

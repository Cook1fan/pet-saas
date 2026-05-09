package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.ActivityOrderCreateReq;
import com.pet.saas.dto.resp.ActivityInfoVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "C端小程序-活动管理")
@RestController
@RequestMapping("/api/user/activity")
@RequiredArgsConstructor
public class UserActivityController {

    private final UserService userService;

    @Operation(summary = "活动列表")
    @GetMapping("/list")
    public R<List<ActivityInfoVO>> getActivityList(
            @Parameter(description = "活动类型：1-拼团，2-秒杀")
            @RequestParam(required = false) Integer type) {
        List<ActivityInfoVO> list = userService.getActivityList(type);
        return R.ok(list);
    }

    @Operation(summary = "活动详情")
    @GetMapping("/detail")
    public R<ActivityInfoVO> getActivityDetail(
            @Parameter(description = "活动ID", required = true)
            @RequestParam @NotNull Long activityId) {
        ActivityInfoVO vo = userService.getActivityDetail(activityId);
        return R.ok(vo);
    }

    @Operation(summary = "活动下单")
    @PostMapping("/order/create")
    public R<Map<String, Object>> createActivityOrder(@Valid @RequestBody ActivityOrderCreateReq req) {
        Map<String, Object> result = userService.createActivityOrder(req);
        return R.ok(result);
    }
}

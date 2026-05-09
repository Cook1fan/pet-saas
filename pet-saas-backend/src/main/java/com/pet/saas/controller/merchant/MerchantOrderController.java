package com.pet.saas.controller.merchant;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.OrderInfoVO;
import com.pet.saas.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商家版小程序-订单管理")
@RestController
@RequestMapping("/api/merchant/order")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantService merchantService;

    @Operation(summary = "今日订单列表")
    @GetMapping("/todayList")
    public R<List<OrderInfoVO>> getTodayOrderList() {
        List<OrderInfoVO> list = merchantService.getTodayOrderList();
        return R.ok(list);
    }
}

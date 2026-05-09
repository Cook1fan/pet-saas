package com.pet.saas.controller.merchant;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.MerchantDashboardVO;
import com.pet.saas.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家版小程序-数据概览")
@RestController
@RequestMapping("/api/merchant/dashboard")
@RequiredArgsConstructor
public class MerchantDashboardController {

    private final MerchantService merchantService;

    @Operation(summary = "数据概览")
    @GetMapping("/overview")
    public R<MerchantDashboardVO> getDashboardOverview() {
        MerchantDashboardVO vo = merchantService.getDashboardOverview();
        return R.ok(vo);
    }
}

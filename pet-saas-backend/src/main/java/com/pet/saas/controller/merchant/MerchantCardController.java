package com.pet.saas.controller.merchant;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.CardVerifyReq;
import com.pet.saas.dto.resp.CardVerifyResultVO;
import com.pet.saas.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家版小程序-次卡核销")
@RestController
@RequestMapping("/api/merchant/card")
@RequiredArgsConstructor
public class MerchantCardController {

    private final MerchantService merchantService;

    @Operation(summary = "次卡核销")
    @PostMapping("/verify")
    public R<CardVerifyResultVO> verifyCard(@Valid @RequestBody CardVerifyReq req) {
        CardVerifyResultVO result = merchantService.verifyMemberCard(req.getVerifyCode());
        return R.ok(result);
    }
}

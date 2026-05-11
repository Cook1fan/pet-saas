package com.pet.saas.controller.merchant;

import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.req.VerifyByCodeReq;
import com.pet.saas.dto.resp.CardVerifyResultVO;
import com.pet.saas.service.CardService;
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

    private final CardService cardService;

    @Operation(summary = "次卡核销")
    @PostMapping("/verify")
    public R<CardVerifyResultVO> verifyCard(@Valid @RequestBody VerifyByCodeReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long operatorId = StpKit.SHOP.getLoginIdAsLong();
        CardVerifyResultVO result = cardService.verifyByCode(req.getVerifyCode(), tenantId, operatorId);
        return R.ok(result);
    }
}

package com.pet.saas.controller.merchant;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.MerchantLoginReq;
import com.pet.saas.dto.resp.LoginRespVO;
import com.pet.saas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家版小程序-登录认证")
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class MerchantAuthController {

    private final AuthService authService;

    @Operation(summary = "商家版登录")
    @PostMapping("/login")
    public R<LoginRespVO> merchantLogin(@Valid @RequestBody MerchantLoginReq req) {
        LoginRespVO result = authService.merchantLogin(req);
        return R.ok(result);
    }

    @Operation(summary = "商家版登出")
    @PostMapping("/logout")
    public R<Void> merchantLogout() {
        authService.merchantLogout();
        return R.ok();
    }
}

package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.WxLoginReq;
import com.pet.saas.dto.resp.WxLoginRespVO;
import com.pet.saas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "C端小程序-登录认证")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final AuthService authService;

    @Operation(summary = "微信授权登录")
    @PostMapping("/wxLogin")
    public R<WxLoginRespVO> wxLogin(@Valid @RequestBody WxLoginReq req) {
        WxLoginRespVO result = authService.wxLogin(req);
        return R.ok(result);
    }

    @Operation(summary = "C端登出")
    @PostMapping("/logout")
    public R<Void> userLogout() {
        authService.userLogout();
        return R.ok();
    }
}

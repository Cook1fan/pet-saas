package com.pet.saas.controller.platform;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.PlatformLoginReq;
import com.pet.saas.dto.resp.LoginRespVO;
import com.pet.saas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "平台管理端-登录认证")
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final AuthService authService;

    @Operation(summary = "平台管理端登录")
    @PostMapping("/login")
    public R<LoginRespVO> platformLogin(@Valid @RequestBody PlatformLoginReq req) {
        LoginRespVO result = authService.platformLogin(req);
        return R.ok(result);
    }

    @Operation(summary = "平台管理端登出")
    @PostMapping("/logout")
    public R<Void> platformLogout() {
        authService.platformLogout();
        return R.ok();
    }
}

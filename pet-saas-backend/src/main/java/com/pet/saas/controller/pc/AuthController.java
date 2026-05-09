package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.PcLoginReq;
import com.pet.saas.dto.resp.LoginRespVO;
import com.pet.saas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "门店PC端-登录认证")
@RestController
@RequestMapping("/api/pc")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "PC端登录")
    @PostMapping("/login")
    public R<LoginRespVO> pcLogin(@Valid @RequestBody PcLoginReq req) {
        LoginRespVO result = authService.pcLogin(req);
        return R.ok(result);
    }

    @Operation(summary = "PC端登出")
    @PostMapping("/logout")
    public R<Void> pcLogout() {
        authService.pcLogout();
        return R.ok();
    }
}

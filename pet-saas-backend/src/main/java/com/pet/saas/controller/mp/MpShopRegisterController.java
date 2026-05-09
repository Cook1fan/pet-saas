package com.pet.saas.controller.mp;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.MpRegisterSubmitReq;
import com.pet.saas.dto.resp.MpRegisterCheckRespVO;
import com.pet.saas.dto.resp.MpRegisterSubmitRespVO;
import com.pet.saas.service.MpShopRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mp/shop")
@RequiredArgsConstructor
@Tag(name = "微信公众号门店注册接口", description = "门店注册等接口")
public class MpShopRegisterController {

    private final MpShopRegisterService mpShopRegisterService;

    @Operation(summary = "微信注册预检查", description = "检查微信是否已注册门店")
    @GetMapping("/register/check")
    public R<MpRegisterCheckRespVO> checkRegister(
            @Parameter(description = "微信授权临时token", required = true) @RequestParam String mpToken) {
        log.info("微信注册预检查请求");
        MpRegisterCheckRespVO resp = mpShopRegisterService.checkRegister(mpToken);
        return R.ok(resp);
    }

    @Operation(summary = "提交门店注册信息", description = "完善门店信息并完成注册")
    @PostMapping("/register/submit")
    public R<MpRegisterSubmitRespVO> submitRegister(@Valid @RequestBody MpRegisterSubmitReq req) {
        log.info("提交门店注册信息请求, shopName={}", req.getShopName());
        MpRegisterSubmitRespVO resp = mpShopRegisterService.submitRegister(req);
        return R.ok(resp);
    }
}

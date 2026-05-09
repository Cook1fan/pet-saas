package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.req.QrCodeBindReq;
import com.pet.saas.dto.resp.QrCodeVerifyVO;
import com.pet.saas.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * C端-二维码接口
 */
@Tag(name = "C端-二维码")
@RestController
@RequestMapping("/api/user/qrcode")
@RequiredArgsConstructor
@Validated
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @Operation(summary = "验证二维码有效性")
    @GetMapping("/verify")
    public R<QrCodeVerifyVO> verifyQrCode(
            @Parameter(description = "从二维码解析出的scene参数", required = true)
            @RequestParam String scene) {
        QrCodeVerifyVO result = qrCodeService.verifyQrCode(scene);
        return R.ok(result);
    }

    @Operation(summary = "扫码绑定（注册/登录后调用）")
    @PostMapping("/bind")
    public R<Void> bindMember(@Validated @RequestBody QrCodeBindReq req) {
        qrCodeService.bindMember(req);
        return R.ok();
    }
}
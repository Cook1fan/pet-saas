package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.QrCodeStatQuery;
import com.pet.saas.dto.resp.QrCodeStatVO;
import com.pet.saas.dto.resp.ShopQrCodeVO;
import com.pet.saas.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * PC端-店铺二维码管理
 * 注意：接口鉴权由 SaTokenConfig 中的拦截器统一处理
 */
@Tag(name = "门店PC端-店铺二维码")
@RestController
@RequestMapping("/api/pc/shop/qrcode")
@RequiredArgsConstructor
public class ShopQrCodeController {

    private final QrCodeService qrCodeService;

    @Operation(summary = "获取店铺二维码")
    @GetMapping
    public R<ShopQrCodeVO> getQrCode(
            @Parameter(description = "租户ID（可选，不传则获取当前商家的）") @RequestParam(required = false) Long tenantId) {
        // 如果没有传tenantId，则获取当前登录商家的
        if (tenantId == null) {
            tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        }
        ShopQrCodeVO vo = qrCodeService.getOrCreateShopQrCode(tenantId);
        return R.ok(vo);
    }

    @Operation(summary = "下载二维码")
    @GetMapping("/download")
    public R<String> downloadQrCode(
            @Parameter(description = "二维码ID", required = true) @RequestParam Long qrCodeId) {
        String qrUrl = qrCodeService.downloadQrCode(qrCodeId);
        return R.ok(qrUrl);
    }

    @Operation(summary = "刷新二维码")
    @PostMapping("/refresh")
    public R<String> refreshQrCode(
            @Parameter(description = "二维码ID", required = true) @RequestParam Long qrCodeId) {
        String newQrUrl = qrCodeService.refreshQrCode(qrCodeId);
        return R.ok(newQrUrl);
    }

    @Operation(summary = "二维码统计")
    @GetMapping("/stat")
    public R<QrCodeStatVO> getQrCodeStat(
            @Parameter(description = "租户ID（可选）") @RequestParam(required = false) Long tenantId,
            @Parameter(description = "开始日期（可选，格式yyyy-MM-dd）")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期（可选，格式yyyy-MM-dd）")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // 如果没有传tenantId，则获取当前登录商家的
        if (tenantId == null) {
            tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        }

        QrCodeStatQuery query = new QrCodeStatQuery();
        query.setTenantId(tenantId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);

        QrCodeStatVO stat = qrCodeService.getQrCodeStat(query);
        return R.ok(stat);
    }
}
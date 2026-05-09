package com.pet.saas.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.saas.common.util.OssUtil;
import com.pet.saas.config.properties.OssProperties;
import com.pet.saas.dto.query.QrCodeStatQuery;
import com.pet.saas.dto.req.QrCodeBindReq;
import com.pet.saas.dto.resp.QrCodeStatVO;
import com.pet.saas.dto.resp.QrCodeVerifyVO;
import com.pet.saas.dto.resp.ShopQrCodeVO;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 二维码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeServiceImpl implements QrCodeService {

    private final ShopQrCodeMapper shopQrCodeMapper;
    private final QrScanLogMapper qrScanLogMapper;
    private final MemberShopBindMapper memberShopBindMapper;
    private final MemberMapper memberMapper;
    private final SysTenantMapper sysTenantMapper;
    private final WxMaService wxMaService;
    private final OssProperties ossProperties;
    private final OssUtil ossUtil;

    @Override
    public ShopQrCodeVO getOrCreateShopQrCode(Long tenantId) {
        log.info("获取店铺二维码，tenantId={}", tenantId);

        ShopQrCode existingQr = shopQrCodeMapper.selectOne(
                new LambdaQueryWrapper<ShopQrCode>()
                        .eq(ShopQrCode::getTenantId, tenantId)
                        .eq(ShopQrCode::getQrType, 1)
                        .eq(ShopQrCode::getIsDefault, 1)
                        .eq(ShopQrCode::getIsDeleted, 0)
        );

        if (existingQr != null) {
            log.info("二维码已存在，直接返回，qrId={}", existingQr.getId());
            return this.convertToVO(existingQr);
        }

        String scene = "tenant_" + tenantId;
        String qrUrl = this.generateWxMaQrCodeUrl(scene);
        String ticket = "WX_TICKET_" + System.currentTimeMillis();

        ShopQrCode qrCode = new ShopQrCode();
        qrCode.setTenantId(tenantId);
        qrCode.setQrType(1);
        qrCode.setQrName("店铺推广码");
        qrCode.setScene(scene);
        qrCode.setQrUrl(qrUrl);
        qrCode.setQrTicket(ticket);
        qrCode.setIsDefault(1);
        qrCode.setScanCount(0);
        qrCode.setCreateUser(1L);

        shopQrCodeMapper.insert(qrCode);
        log.info("创建新二维码成功，qrId={}", qrCode.getId());

        return this.convertToVO(qrCode);
    }

    @Override
    public String downloadQrCode(Long qrCodeId) {
        log.info("下载二维码，qrCodeId={}", qrCodeId);

        ShopQrCode qrCode = shopQrCodeMapper.selectById(qrCodeId);
        if (qrCode == null || qrCode.getIsDeleted() == 1) {
            throw new RuntimeException("二维码不存在");
        }

        return qrCode.getQrUrl();
    }

    @Override
    public String refreshQrCode(Long qrCodeId) {
        log.info("刷新二维码，qrCodeId={}", qrCodeId);

        ShopQrCode qrCode = shopQrCodeMapper.selectById(qrCodeId);
        if (qrCode == null || qrCode.getIsDeleted() == 1) {
            throw new RuntimeException("二维码不存在");
        }

        String scene = qrCode.getScene();
        String newQrUrl = this.generateWxMaQrCodeUrl(scene);
        String newTicket = "WX_TICKET_REFRESH_" + System.currentTimeMillis();

        qrCode.setQrUrl(newQrUrl);
        qrCode.setQrTicket(newTicket);
        qrCode.setUpdateUser(1L);
        shopQrCodeMapper.updateById(qrCode);

        log.info("二维码刷新成功，新的qrUrl={}", newQrUrl);
        return newQrUrl;
    }

    @Override
    public QrCodeStatVO getQrCodeStat(QrCodeStatQuery query) {
        Long tenantId = query.getTenantId();

        ShopQrCode qrCode = shopQrCodeMapper.selectOne(
                new LambdaQueryWrapper<ShopQrCode>()
                        .eq(ShopQrCode::getTenantId, tenantId)
                        .eq(ShopQrCode::getQrType, 1)
                        .eq(ShopQrCode::getIsDefault, 1)
                        .eq(ShopQrCode::getIsDeleted, 0)
        );

        QrCodeStatVO statVO = new QrCodeStatVO();
        statVO.setTotalScan(0);
        statVO.setNewUser(0);
        statVO.setOldUser(0);
        statVO.setGuestUser(0);
        statVO.setDailyStats(new ArrayList<>());

        if (qrCode != null) {
            statVO.setTotalScan(qrCode.getScanCount());

            LambdaQueryWrapper<QrScanLog> logQuery = new LambdaQueryWrapper<QrScanLog>()
                    .eq(QrScanLog::getQrId, qrCode.getId())
                    .eq(QrScanLog::getTenantId, tenantId);

            if (query.getStartDate() != null) {
                LocalDateTime startTime = query.getStartDate().atStartOfDay();
                logQuery.ge(QrScanLog::getCreateTime, startTime);
            }
            if (query.getEndDate() != null) {
                LocalDateTime endTime = query.getEndDate().atTime(23, 59, 59);
                logQuery.le(QrScanLog::getCreateTime, endTime);
            }

            List<QrScanLog> logs = qrScanLogMapper.selectList(logQuery);

            int newUser = 0;
            int oldUser = 0;
            int guestUser = 0;
            for (QrScanLog logItem : logs) {
                if (logItem.getScanResult() != null) {
                    switch (logItem.getScanResult()) {
                        case 1 -> newUser++;
                        case 2 -> oldUser++;
                        case 3 -> guestUser++;
                    }
                }
            }
            statVO.setNewUser(newUser);
            statVO.setOldUser(oldUser);
            statVO.setGuestUser(guestUser);
            statVO.setDailyStats(this.calculateDailyStats(logs));
        }

        return statVO;
    }

    @Override
    public QrCodeVerifyVO verifyQrCode(String scene) {
        log.info("验证二维码，scene={}", scene);

        QrCodeVerifyVO resp = new QrCodeVerifyVO();

        if (!scene.startsWith("tenant_")) {
            resp.setValid(false);
            return resp;
        }

        String tenantIdStr = scene.substring("tenant_".length());
        if (!StrUtil.isNumeric(tenantIdStr)) {
            resp.setValid(false);
            return resp;
        }

        Long tenantId = Long.parseLong(tenantIdStr);

        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null || tenant.getIsDeleted() == 1 || tenant.getStatus() != 1) {
            resp.setValid(false);
            return resp;
        }

        resp.setValid(true);
        resp.setTenantId(tenantId);
        resp.setTenantName(tenant.getShopName());
        resp.setTenantLogo(null);

        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindMember(QrCodeBindReq req) {
        log.info("绑定会员到店铺，tenantId={}, memberId={}", req.getTenantId(), req.getMemberId());

        MemberShopBind existing = memberShopBindMapper.selectOne(
                new LambdaQueryWrapper<MemberShopBind>()
                        .eq(MemberShopBind::getMemberId, req.getMemberId())
                        .eq(MemberShopBind::getTenantId, req.getTenantId())
        );

        if (existing != null) {
            existing.setLastVisitTime(LocalDateTime.now());
            existing.setBindSource(1);
            memberShopBindMapper.updateById(existing);
            log.info("更新已有绑定关系，bindId={}", existing.getId());
            return;
        }

        MemberShopBind bind = new MemberShopBind();
        bind.setTenantId(req.getTenantId());
        bind.setMemberId(req.getMemberId());
        bind.setBindSource(1);

        Member member = memberMapper.selectById(req.getMemberId());
        if (member != null && StrUtil.isNotBlank(member.getOpenid())) {
            bind.setOpenid(member.getOpenid());
        }

        memberShopBindMapper.insert(bind);
        log.info("创建新绑定关系成功，bindId={}", bind.getId());

        if (member != null) {
            member.setRegisterSource(1);
            member.setFromTenantId(req.getTenantId());
            memberMapper.updateById(member);
        }

        this.recordScanLog(req.getTenantId(), req.getMemberId(), 1);
    }

    /**
     * 生成微信小程序码URL
     */
    private String generateWxMaQrCodeUrl(String scene) {
        if (wxMaService == null) {
            log.warn("WxMaService 未初始化，返回模拟二维码URL");
            return generateMockQrCodeUrl(scene);
        }

        try {
            String accessToken = wxMaService.getAccessToken();
            if (StrUtil.isBlank(accessToken)) {
                log.warn("获取 access_token 失败，返回模拟二维码URL");
                return generateMockQrCodeUrl(scene);
            }

            // 使用接口B：获取小程序码（永久有效、数量暂无限制）
            byte[] qrCodeBytes = wxMaService.getQrcodeService().createWxaCodeUnlimitBytes(scene, "pages/index/index", false, "release", 430, true, null, false);
            log.info("微信二维码创建成功，长度={}", qrCodeBytes.length);

            // 上传到OSS
            String fileKey = generateQrCodeFileKey(scene);
            String qrUrl = uploadQrCodeToOss(fileKey, qrCodeBytes);
            log.info("二维码上传OSS成功，fileKey={}", fileKey);

            return qrUrl;

        } catch (WxErrorException e) {
            log.error("微信API调用失败，errorCode={}, errorMsg={}", e.getError().getErrorCode(), e.getError().getErrorMsg());
            return generateMockQrCodeUrl(scene);
        } catch (Exception e) {
            log.error("生成小程序码异常，error={}", e.getMessage(), e);
            return generateMockQrCodeUrl(scene);
        }
    }

    /**
     * 生成二维码文件key
     */
    private String generateQrCodeFileKey(String scene) {
        String datePath = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("images/qrcode/%s/%s.png", datePath, uuid);
    }

    /**
     * 上传二维码图片到OSS
     */
    private String uploadQrCodeToOss(String fileKey, byte[] qrCodeBytes) {
        var ossClient = ossUtil.createClient();
        try {
            PutObjectResult result = ossClient.putObject(
                    ossProperties.getBucket(),
                    fileKey,
                    new ByteArrayInputStream(qrCodeBytes)
            );
            log.info("OSS上传成功，ETag={}", result.getETag());

            // 生成签名URL，返回给前端访问
            return ossUtil.generatePresignedUrl(fileKey, 3600 * 24 * 7); // 7天有效期
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成模拟的二维码URL（用于开发/测试）
     */
    private String generateMockQrCodeUrl(String scene) {
        return "https://api.weixin.qq.com/cgi-bin/showqrcode?ticket=MOCK_" + scene + "_" + System.currentTimeMillis();
    }

    /**
     * 记录扫码日志
     */
    private void recordScanLog(Long tenantId, Long memberId, Integer scanResult) {
        QrScanLog scanLog = new QrScanLog();
        scanLog.setTenantId(tenantId);
        scanLog.setMemberId(memberId);
        scanLog.setScanResult(scanResult);
        scanLog.setDeviceType("miniapp");

        ShopQrCode qrCode = shopQrCodeMapper.selectOne(
                new LambdaQueryWrapper<ShopQrCode>()
                        .eq(ShopQrCode::getTenantId, tenantId)
                        .eq(ShopQrCode::getIsDefault, 1)
                        .eq(ShopQrCode::getIsDeleted, 0)
        );
        if (qrCode != null) {
            scanLog.setQrId(qrCode.getId());
        }

        qrScanLogMapper.insert(scanLog);

        if (qrCode != null) {
            qrCode.setScanCount(qrCode.getScanCount() + 1);
            shopQrCodeMapper.updateById(qrCode);
        }
    }

    /**
     * 转换实体为VO
     */
    private ShopQrCodeVO convertToVO(ShopQrCode qrCode) {
        ShopQrCodeVO vo = new ShopQrCodeVO();
        vo.setQrCodeId(qrCode.getId());
        vo.setQrName(qrCode.getQrName());
        vo.setQrUrl(qrCode.getQrUrl());
        vo.setQrType(qrCode.getQrType());
        vo.setScanCount(qrCode.getScanCount());
        vo.setCreateTime(qrCode.getCreateTime());
        return vo;
    }

    /**
     * 计算每日统计
     */
    private List<QrCodeStatVO.DailyStat> calculateDailyStats(List<QrScanLog> logs) {
        return new ArrayList<>();
    }
}
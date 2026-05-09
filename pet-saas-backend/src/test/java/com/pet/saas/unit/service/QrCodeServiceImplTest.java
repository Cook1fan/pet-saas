package com.pet.saas.unit.service;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.config.properties.WxProperties;
import com.pet.saas.dto.query.QrCodeStatQuery;
import com.pet.saas.dto.req.QrCodeBindReq;
import com.pet.saas.dto.resp.QrCodeStatVO;
import com.pet.saas.dto.resp.QrCodeVerifyVO;
import com.pet.saas.dto.resp.ShopQrCodeVO;
import com.pet.saas.entity.Member;
import com.pet.saas.entity.MemberShopBind;
import com.pet.saas.entity.QrScanLog;
import com.pet.saas.entity.ShopQrCode;
import com.pet.saas.entity.SysTenant;
import com.pet.saas.mapper.MemberMapper;
import com.pet.saas.mapper.MemberShopBindMapper;
import com.pet.saas.mapper.QrScanLogMapper;
import com.pet.saas.mapper.ShopQrCodeMapper;
import com.pet.saas.mapper.SysTenantMapper;
import com.pet.saas.service.impl.QrCodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 二维码服务测试
 */
@DisplayName("二维码服务测试")
@MockitoSettings(strictness = Strictness.LENIENT)
class QrCodeServiceImplTest extends BaseUnitTest {

    @Mock
    private ShopQrCodeMapper shopQrCodeMapper;

    @Mock
    private QrScanLogMapper qrScanLogMapper;

    @Mock
    private MemberShopBindMapper memberShopBindMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private SysTenantMapper sysTenantMapper;

    @Mock
    private WxProperties wxProperties;

    @InjectMocks
    private QrCodeServiceImpl qrCodeService;

    @Test
    @DisplayName("获取店铺二维码 - 当已存在时直接返回")
    void getOrCreateShopQrCode_whenExists_returnExisting() {
        Long tenantId = 1L;
        ShopQrCode existingQr = createShopQrCode(1L, tenantId);

        when(shopQrCodeMapper.selectOne(any())).thenReturn(existingQr);

        ShopQrCodeVO result = qrCodeService.getOrCreateShopQrCode(tenantId);

        assertNotNull(result);
        assertEquals(existingQr.getId(), result.getQrCodeId());
        assertEquals(existingQr.getQrUrl(), result.getQrUrl());
        verify(shopQrCodeMapper, never()).insert(any());
    }

    @Test
    @DisplayName("获取店铺二维码 - 当不存在时创建新二维码")
    void getOrCreateShopQrCode_whenNotExists_createNew() {
        Long tenantId = 1L;

        when(shopQrCodeMapper.selectOne(any())).thenReturn(null);
        when(wxProperties.getAppid()).thenReturn("test-appid");
        when(shopQrCodeMapper.insert(any())).thenAnswer(inv -> {
            ShopQrCode qr = inv.getArgument(0);
            qr.setId(1L);
            return 1;
        });

        ShopQrCodeVO result = qrCodeService.getOrCreateShopQrCode(tenantId);

        assertNotNull(result);
        assertEquals(1L, result.getQrCodeId());
        assertEquals("店铺推广码", result.getQrName());
        verify(shopQrCodeMapper, times(1)).insert(any());
    }

    @Test
    @DisplayName("下载二维码 - 当不存在时抛出异常")
    void downloadQrCode_whenNotExists_throwException() {
        when(shopQrCodeMapper.selectById(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                qrCodeService.downloadQrCode(1L));

        assertEquals("二维码不存在", exception.getMessage());
    }

    @Test
    @DisplayName("下载二维码 - 当存在时返回URL")
    void downloadQrCode_whenExists_returnUrl() {
        ShopQrCode qrCode = createShopQrCode(1L, 1L);
        qrCode.setQrUrl("https://example.com/qr.png");

        when(shopQrCodeMapper.selectById(1L)).thenReturn(qrCode);

        String result = qrCodeService.downloadQrCode(1L);

        assertEquals("https://example.com/qr.png", result);
    }

    @Test
    @DisplayName("刷新二维码 - 当不存在时抛出异常")
    void refreshQrCode_whenNotExists_throwException() {
        when(shopQrCodeMapper.selectById(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                qrCodeService.refreshQrCode(1L));

        assertEquals("二维码不存在", exception.getMessage());
    }

    @Test
    @DisplayName("刷新二维码 - 当存在时生成新URL")
    void refreshQrCode_whenExists_generateNewUrl() {
        ShopQrCode qrCode = createShopQrCode(1L, 1L);
        qrCode.setScene("tenant_1");

        when(shopQrCodeMapper.selectById(1L)).thenReturn(qrCode);
        when(wxProperties.getAppid()).thenReturn("test-appid");

        String result = qrCodeService.refreshQrCode(1L);

        assertNotNull(result);
        verify(shopQrCodeMapper, times(1)).updateById(any());
    }

    @Test
    @DisplayName("验证二维码 - scene格式无效时返回无效")
    void verifyQrCode_withInvalidScene_returnInvalid() {
        QrCodeVerifyVO result = qrCodeService.verifyQrCode("invalid_scene");

        assertFalse(result.getValid());
    }

    @Test
    @DisplayName("验证二维码 - scene格式有效但租户不存在时返回无效")
    void verifyQrCode_withValidSceneButNoTenant_returnInvalid() {
        when(sysTenantMapper.selectById(1L)).thenReturn(null);

        QrCodeVerifyVO result = qrCodeService.verifyQrCode("tenant_1");

        assertFalse(result.getValid());
    }

    @Test
    @DisplayName("验证二维码 - scene格式有效且租户存在时返回有效")
    void verifyQrCode_withValidSceneAndTenant_returnValid() {
        SysTenant tenant = new SysTenant();
        tenant.setTenantId(1L);
        tenant.setShopName("测试门店");
        tenant.setIsDeleted(0);
        tenant.setStatus(1);

        when(sysTenantMapper.selectById(1L)).thenReturn(tenant);

        QrCodeVerifyVO result = qrCodeService.verifyQrCode("tenant_1");

        assertTrue(result.getValid());
        assertEquals(1L, result.getTenantId());
        assertEquals("测试门店", result.getTenantName());
    }

    @Test
    @DisplayName("绑定会员 - 当已存在绑定时更新最后访问时间")
    void bindMember_whenExists_updateLastVisitTime() {
        QrCodeBindReq req = new QrCodeBindReq();
        req.setTenantId(1L);
        req.setMemberId(1L);

        MemberShopBind existing = new MemberShopBind();
        existing.setId(1L);
        existing.setTenantId(1L);
        existing.setMemberId(1L);

        when(memberShopBindMapper.selectOne(any())).thenReturn(existing);
        when(shopQrCodeMapper.selectOne(any())).thenReturn(null);

        assertDoesNotThrow(() -> qrCodeService.bindMember(req));

        verify(memberShopBindMapper, times(1)).updateById(any());
        verify(memberShopBindMapper, never()).insert(any());
    }

    @Test
    @DisplayName("绑定会员 - 当不存在绑定时创建新绑定")
    void bindMember_whenNotExists_createNew() {
        QrCodeBindReq req = new QrCodeBindReq();
        req.setTenantId(1L);
        req.setMemberId(1L);

        Member member = new Member();
        member.setId(1L);
        member.setOpenid("test_openid");

        ShopQrCode qrCode = createShopQrCode(1L, 1L);

        when(memberShopBindMapper.selectOne(any())).thenReturn(null);
        when(memberMapper.selectById(1L)).thenReturn(member);
        when(shopQrCodeMapper.selectOne(any())).thenReturn(qrCode);

        assertDoesNotThrow(() -> qrCodeService.bindMember(req));

        verify(memberShopBindMapper, times(1)).insert(any());
        verify(memberMapper, times(1)).updateById(any());
    }

    @Test
    @DisplayName("获取二维码统计 - 返回正确的统计数据")
    void getQrCodeStat_returnCorrectStatistics() {
        QrCodeStatQuery query = new QrCodeStatQuery();
        query.setTenantId(1L);
        query.setStartDate(LocalDate.now().minusDays(7));
        query.setEndDate(LocalDate.now());

        ShopQrCode qrCode = createShopQrCode(1L, 1L);
        qrCode.setScanCount(10);

        QrScanLog log1 = createScanLog(1L, 1L, 1);
        QrScanLog log2 = createScanLog(1L, 1L, 2);

        when(shopQrCodeMapper.selectOne(any())).thenReturn(qrCode);
        when(qrScanLogMapper.selectList(any())).thenReturn(List.of(log1, log2));

        QrCodeStatVO result = qrCodeService.getQrCodeStat(query);

        assertNotNull(result);
        assertEquals(10, result.getTotalScan());
        assertEquals(1, result.getNewUser());
        assertEquals(1, result.getOldUser());
        assertEquals(0, result.getGuestUser());
    }

    @Test
    @DisplayName("获取二维码统计 - 当租户无二维码时返回零值")
    void getQrCodeStat_whenNoQrCode_returnZeros() {
        QrCodeStatQuery query = new QrCodeStatQuery();
        query.setTenantId(1L);

        when(shopQrCodeMapper.selectOne(any())).thenReturn(null);

        QrCodeStatVO result = qrCodeService.getQrCodeStat(query);

        assertNotNull(result);
        assertEquals(0, result.getTotalScan());
        assertEquals(0, result.getNewUser());
        assertEquals(0, result.getOldUser());
        assertEquals(0, result.getGuestUser());
    }

    @Test
    @DisplayName("获取二维码统计 - 当无扫码日志时返回零值")
    void getQrCodeStat_whenNoScanLogs_returnZeros() {
        QrCodeStatQuery query = new QrCodeStatQuery();
        query.setTenantId(1L);

        ShopQrCode qrCode = createShopQrCode(1L, 1L);
        qrCode.setScanCount(0);

        when(shopQrCodeMapper.selectOne(any())).thenReturn(qrCode);
        when(qrScanLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        QrCodeStatVO result = qrCodeService.getQrCodeStat(query);

        assertNotNull(result);
        assertEquals(0, result.getTotalScan());
        assertEquals(0, result.getNewUser());
        assertEquals(0, result.getOldUser());
        assertEquals(0, result.getGuestUser());
    }

    // ========== Helper Methods ==========

    private ShopQrCode createShopQrCode(Long qrId, Long tenantId) {
        ShopQrCode qrCode = new ShopQrCode();
        qrCode.setId(qrId);
        qrCode.setTenantId(tenantId);
        qrCode.setQrType(1);
        qrCode.setQrName("店铺推广码");
        qrCode.setScene("tenant_" + tenantId);
        qrCode.setQrUrl("https://example.com/qr.png");
        qrCode.setQrTicket("test_ticket");
        qrCode.setIsDefault(1);
        qrCode.setScanCount(0);
        qrCode.setCreateUser(1L);
        qrCode.setCreateTime(LocalDateTime.now());
        qrCode.setIsDeleted(0);
        return qrCode;
    }

    private QrScanLog createScanLog(Long id, Long tenantId, Integer scanResult) {
        QrScanLog log = new QrScanLog();
        log.setId(id);
        log.setQrId(1L);
        log.setTenantId(tenantId);
        log.setScanResult(scanResult);
        log.setDeviceType("miniapp");
        log.setCreateTime(LocalDateTime.now());
        return log;
    }
}
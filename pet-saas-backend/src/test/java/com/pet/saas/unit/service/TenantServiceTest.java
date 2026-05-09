package com.pet.saas.unit.service;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.dto.req.CreateTenantReq;
import com.pet.saas.dto.req.UpdateTenantStatusReq;
import com.pet.saas.dto.resp.CreateTenantRespVO;
import com.pet.saas.entity.ShopAdmin;
import com.pet.saas.entity.ShopConfig;
import com.pet.saas.entity.SysTenant;
import com.pet.saas.mapper.ShopAdminMapper;
import com.pet.saas.mapper.ShopConfigMapper;
import com.pet.saas.mapper.SysTenantMapper;
import com.pet.saas.service.TenantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("租户服务测试")
class TenantServiceTest extends BaseUnitTest {

    @Mock
    private SysTenantMapper sysTenantMapper;

    @Mock
    private ShopAdminMapper shopAdminMapper;

    @Mock
    private ShopConfigMapper shopConfigMapper;

    @InjectMocks
    private TenantService tenantService;

    @Test
    @DisplayName("应该成功创建租户")
    void shouldCreateTenantSuccessfully() {
        when(sysTenantMapper.insert(any(SysTenant.class))).thenAnswer(inv -> {
            SysTenant tenant = inv.getArgument(0);
            tenant.setTenantId(1L);
            return 1;
        });
        when(shopAdminMapper.insert(any(ShopAdmin.class))).thenReturn(1);
        when(shopConfigMapper.insert(any(ShopConfig.class))).thenReturn(1);

        CreateTenantReq req = new CreateTenantReq();
        req.setShopName("测试门店");
        req.setAdminPhone("13800138000");
        req.setAddress("测试地址");

        CreateTenantRespVO result = tenantService.createTenant(req);

        assertNotNull(result);
        assertEquals(1L, result.getTenantId());
        assertEquals("13800138000", result.getUsername());
        verify(sysTenantMapper, times(1)).insert(any(SysTenant.class));
        verify(shopAdminMapper, times(1)).insert(any(ShopAdmin.class));
        verify(shopConfigMapper, times(1)).insert(any(ShopConfig.class));
    }

    @Test
    @DisplayName("应该更新租户状态")
    void shouldUpdateTenantStatusSuccessfully() {
        SysTenant tenant = new SysTenant();
        tenant.setTenantId(1L);
        tenant.setStatus(1);
        when(sysTenantMapper.selectById(1L)).thenReturn(tenant);

        UpdateTenantStatusReq req = new UpdateTenantStatusReq();
        req.setTenantId(1L);
        req.setStatus(0);
        req.setResetPassword(false);

        assertDoesNotThrow(() -> tenantService.updateTenantStatus(req));

        verify(sysTenantMapper, times(1)).updateById(any(SysTenant.class));
    }

    @Test
    @DisplayName("当租户不存在时应该抛出异常")
    void shouldThrowExceptionWhenTenantNotExist() {
        when(sysTenantMapper.selectById(999L)).thenReturn(null);

        UpdateTenantStatusReq req = new UpdateTenantStatusReq();
        req.setTenantId(999L);
        req.setStatus(0);
        req.setResetPassword(false);

        assertThrows(RuntimeException.class, () ->
                tenantService.updateTenantStatus(req));
    }
}

package com.pet.saas.unit;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.handler.CustomTenantLineHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("多租户隔离测试")
class TenantIsolationTest extends BaseUnitTest {

    private CustomTenantLineHandler tenantLineHandler;

    @BeforeEach
    void setUp() {
        tenantLineHandler = new CustomTenantLineHandler();
    }

    @Test
    @DisplayName("应该返回正确的租户ID列名")
    void shouldReturnCorrectTenantIdColumn() {
        assertEquals("tenant_id", tenantLineHandler.getTenantIdColumn());
    }

    @Test
    @DisplayName("应该正确忽略全局表")
    void shouldIgnoreGlobalTables() {
        assertTrue(tenantLineHandler.ignoreTable("sys_platform_admin"));
        assertTrue(tenantLineHandler.ignoreTable("sys_tenant"));
        assertTrue(tenantLineHandler.ignoreTable("sys_ai_package"));
        assertTrue(tenantLineHandler.ignoreTable("sys_config"));
    }

    @Test
    @DisplayName("不应该忽略业务表")
    void shouldNotIgnoreBusinessTables() {
        assertFalse(tenantLineHandler.ignoreTable("member"));
        assertFalse(tenantLineHandler.ignoreTable("order_info"));
        assertFalse(tenantLineHandler.ignoreTable("goods"));
    }
}

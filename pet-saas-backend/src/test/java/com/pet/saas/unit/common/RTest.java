package com.pet.saas.unit.common;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.common.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("统一响应封装测试")
class RTest extends BaseUnitTest {

    @Test
    @DisplayName("应该成功创建成功响应")
    void shouldCreateSuccessResponse() {
        R<String> result = R.ok("test data");

        assertNotNull(result);
        assertEquals(R.SUCCESS_CODE, result.getCode());
        assertEquals(R.SUCCESS_MESSAGE, result.getMessage());
        assertEquals("test data", result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("应该成功创建无数据成功响应")
    void shouldCreateSuccessResponseWithoutData() {
        R<Void> result = R.ok();

        assertNotNull(result);
        assertEquals(R.SUCCESS_CODE, result.getCode());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("应该成功创建错误响应")
    void shouldCreateErrorResponse() {
        R<Void> result = R.error("测试错误");

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("测试错误", result.getMessage());
        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("应该成功创建自定义错误码响应")
    void shouldCreateErrorResponseWithCustomCode() {
        R<Void> result = R.error(400, "参数错误");

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertFalse(result.isSuccess());
    }
}

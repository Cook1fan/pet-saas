package com.pet.saas.unit.service;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.common.enums.ActivityTypeEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.dto.req.CreateGroupActivityReq;
import com.pet.saas.dto.resp.CreateGroupActivityResp;
import com.pet.saas.entity.ActivityInfo;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.mapper.*;
import com.pet.saas.service.FlowRecordService;
import com.pet.saas.service.WechatPayService;
import com.pet.saas.service.impl.GroupActivityServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("拼团服务测试")
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupActivityServiceTest extends BaseUnitTest {

    @Mock
    private ActivityInfoMapper activityInfoMapper;

    @Mock
    private ActivityGroupMapper activityGroupMapper;

    @Mock
    private ActivityOrderMapper activityOrderMapper;

    @Mock
    private GoodsSkuMapper goodsSkuMapper;

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private WechatPayService wechatPayService;

    @Mock
    private FlowRecordService flowRecordService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private GroupActivityServiceImpl groupActivityService;

    @Test
    @DisplayName("应该成功创建拼团活动")
    void shouldCreateGroupActivitySuccessfully() {
        CreateGroupActivityReq req = new CreateGroupActivityReq();
        req.setTitle("猫粮2人拼团");
        req.setGoodsId(1L);
        req.setSkuId(1L);
        req.setPrice(new BigDecimal("49.90"));
        req.setOriginPrice(new BigDecimal("99.00"));
        req.setGroupCount(2);
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(2);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(7));

        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 100);
        when(goodsSkuMapper.selectById(1L)).thenReturn(sku);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(activityInfoMapper.insert(any(ActivityInfo.class))).thenAnswer(inv -> {
            ActivityInfo activity = inv.getArgument(0);
            activity.setId(1L);
            return 1;
        });

        CreateGroupActivityResp resp = groupActivityService.createActivity(req, 1L, 1L);

        assertNotNull(resp);
        assertNotNull(resp.getActivityId());
        verify(activityInfoMapper, times(1)).insert(any(ActivityInfo.class));
    }

    @Test
    @DisplayName("应该拒绝成团人数小于2的活动")
    void shouldRejectWhenGroupCountLessThan2() {
        CreateGroupActivityReq req = new CreateGroupActivityReq();
        req.setTitle("测试");
        req.setGoodsId(1L);
        req.setSkuId(1L);
        req.setPrice(new BigDecimal("10.00"));
        req.setOriginPrice(new BigDecimal("20.00"));
        req.setGroupCount(1); // 小于2
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(1);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(7));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.createActivity(req, 1L, 1L));

        assertEquals("成团人数必须在2-10人之间", exception.getMessage());
    }

    private ActivityInfo createTestActivity() {
        ActivityInfo activity = new ActivityInfo();
        activity.setId(1L);
        activity.setTenantId(1L);
        activity.setTitle("猫粮2人拼团");
        activity.setGoodsId(1L);
        activity.setSkuId(1L);
        activity.setPrice(new BigDecimal("49.90"));
        activity.setOriginPrice(new BigDecimal("99.00"));
        activity.setType(ActivityTypeEnum.GROUP_BUY.getCode());
        activity.setGroupCount(2);
        activity.setGroupValidHours(24);
        activity.setStock(100);
        activity.setLimitNum(2);
        activity.setStatus(1);
        activity.setStartTime(LocalDateTime.now().minusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(6));
        return activity;
    }

    private GoodsSku createTestSku(Long id, BigDecimal price, Integer stock) {
        GoodsSku sku = new GoodsSku();
        sku.setId(id);
        sku.setGoodsId(id);
        sku.setPrice(price);
        sku.setStock(stock);
        sku.setIsUnlimitedStock(0);
        sku.setSpecName("规格");
        sku.setSpecValue("默认");
        return sku;
    }
}

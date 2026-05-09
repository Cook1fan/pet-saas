package com.pet.saas.unit.service;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.common.enums.ActivityGroupStatusEnum;
import com.pet.saas.common.enums.ActivityOrderStatusEnum;
import com.pet.saas.common.enums.ActivityTypeEnum;
import com.pet.saas.common.enums.PayStatusEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.dto.req.CreateGroupActivityReq;
import com.pet.saas.dto.req.JoinGroupReq;
import com.pet.saas.dto.req.LaunchGroupReq;
import com.pet.saas.dto.resp.CreateGroupActivityResp;
import com.pet.saas.dto.resp.CreateGroupOrderResp;
import com.pet.saas.dto.resp.UserGroupDetailResp;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.FlowRecordService;
import com.pet.saas.service.WechatPayService;
import com.pet.saas.service.impl.GroupActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("拼团服务完整测试")
class GroupActivityServiceCompleteTest extends BaseUnitTest {

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

    @Mock
    private RLock rLock;

    @InjectMocks
    private GroupActivityServiceImpl groupActivityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
    }

    // ==================== 商家端测试 ====================

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

        when(activityInfoMapper.insert(any(ActivityInfo.class))).thenAnswer(inv -> {
            ActivityInfo activity = inv.getArgument(0);
            activity.setId(1L);
            return 1;
        });

        CreateGroupActivityResp resp = groupActivityService.createActivity(req, 1L, 1L);

        assertNotNull(resp);
        assertNotNull(resp.getActivityId());
        verify(activityInfoMapper, times(1)).insert(any(ActivityInfo.class));
        verify(redisTemplate.opsForValue()).set(anyString(), eq(100));
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
        req.setGroupCount(1);
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(1);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(7));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.createActivity(req, 1L, 1L));

        assertEquals("成团人数必须在2-10人之间", exception.getMessage());
    }

    @Test
    @DisplayName("应该拒绝成团人数大于10的活动")
    void shouldRejectWhenGroupCountMoreThan10() {
        CreateGroupActivityReq req = new CreateGroupActivityReq();
        req.setTitle("测试");
        req.setGoodsId(1L);
        req.setSkuId(1L);
        req.setPrice(new BigDecimal("10.00"));
        req.setOriginPrice(new BigDecimal("20.00"));
        req.setGroupCount(11);
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(1);
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(7));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.createActivity(req, 1L, 1L));

        assertEquals("成团人数必须在2-10人之间", exception.getMessage());
    }

    @Test
    @DisplayName("应该拒绝结束时间早于开始时间的活动")
    void shouldRejectWhenEndTimeBeforeStartTime() {
        CreateGroupActivityReq req = new CreateGroupActivityReq();
        req.setTitle("测试");
        req.setGoodsId(1L);
        req.setSkuId(1L);
        req.setPrice(new BigDecimal("10.00"));
        req.setOriginPrice(new BigDecimal("20.00"));
        req.setGroupCount(2);
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(1);
        req.setStartTime(LocalDateTime.now().plusDays(7));
        req.setEndTime(LocalDateTime.now().plusDays(1));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.createActivity(req, 1L, 1L));

        assertEquals("开始时间不能晚于结束时间", exception.getMessage());
    }

    // ==================== C端用户测试 ====================

    @Test
    @DisplayName("应该成功发起拼团")
    void shouldLaunchGroupSuccessfully() throws InterruptedException {
        Long tenantId = 1L;
        Long memberId = 1L;
        Long activityId = 1L;

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 100);
        when(goodsSkuMapper.selectById(1L)).thenReturn(sku);

        when(redisTemplate.opsForValue().decrement(anyString(), anyInt())).thenReturn(99L);
        when(rLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(orderInfoMapper.insert(any(OrderInfo.class))).thenAnswer(inv -> {
            OrderInfo order = inv.getArgument(0);
            order.setId(1L);
            order.setOrderNo("ORD2026050900001");
            return 1;
        });

        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(activityOrderMapper.insert(any(ActivityOrder.class))).thenReturn(1);

        Map<String, String> payParams = new HashMap<>();
        payParams.put("timeStamp", "123456789");
        when(wechatPayService.createMiniProgramPayOrder(any(OrderInfo.class))).thenReturn(payParams);

        LaunchGroupReq req = new LaunchGroupReq();
        req.setActivityId(activityId);
        req.setNum(1);

        CreateGroupOrderResp resp = groupActivityService.launchGroup(req, tenantId, memberId);

        assertNotNull(resp);
        assertNotNull(resp.getOrderId());
        assertNotNull(resp.getOrderNo());
        assertEquals(new BigDecimal("49.90"), resp.getPayAmount());
        verify(redisTemplate.opsForValue()).decrement(anyString(), eq(1));
    }

    @Test
    @DisplayName("应该拒绝库存不足的发起拼团")
    void shouldRejectWhenStockInsufficient() {
        Long tenantId = 1L;
        Long memberId = 1L;
        Long activityId = 1L;

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        when(redisTemplate.opsForValue().decrement(anyString(), anyInt())).thenReturn(-1L);

        LaunchGroupReq req = new LaunchGroupReq();
        req.setActivityId(activityId);
        req.setNum(1);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.launchGroup(req, tenantId, memberId));

        assertTrue(exception.getMessage().contains("库存不足"));
        verify(redisTemplate.opsForValue()).increment(anyString(), eq(1));
    }

    @Test
    @DisplayName("应该拒绝重复参与拼团")
    void shouldRejectWhenUserAlreadyInGroup() {
        Long tenantId = 1L;
        Long memberId = 1L;
        Long activityId = 1L;

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setMemberId(memberId);
        activityOrder.setGroupId(1L);
        activityOrder.setStatus(ActivityOrderStatusEnum.PAID.getCode());

        when(activityOrderMapper.selectList(any())).thenReturn(Collections.singletonList(activityOrder));

        ActivityGroup group = new ActivityGroup();
        group.setId(1L);
        group.setStatus(ActivityGroupStatusEnum.GROUPING.getCode());
        when(activityGroupMapper.selectById(1L)).thenReturn(group);

        when(redisTemplate.opsForValue().decrement(anyString(), anyInt())).thenReturn(99L);

        LaunchGroupReq req = new LaunchGroupReq();
        req.setActivityId(activityId);
        req.setNum(1);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.launchGroup(req, tenantId, memberId));

        assertEquals("您已在该活动的拼团中，不能重复参与", exception.getMessage());
    }

    @Test
    @DisplayName("应该成功加入拼团")
    void shouldJoinGroupSuccessfully() throws InterruptedException {
        Long tenantId = 1L;
        Long memberId = 2L;
        Long activityId = 1L;
        Long groupId = 1L;

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        ActivityGroup group = createTestGroup(groupId, activityId, tenantId);
        when(activityGroupMapper.selectById(groupId)).thenReturn(group);

        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 100);
        when(goodsSkuMapper.selectById(1L)).thenReturn(sku);

        when(redisTemplate.opsForValue().decrement(anyString(), anyInt())).thenReturn(98L);
        when(rLock.tryLock(3, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        when(orderInfoMapper.insert(any(OrderInfo.class))).thenAnswer(inv -> {
            OrderInfo order = inv.getArgument(0);
            order.setId(2L);
            order.setOrderNo("ORD2026050900002");
            return 1;
        });

        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        when(activityOrderMapper.insert(any(ActivityOrder.class))).thenReturn(1);

        Map<String, String> payParams = new HashMap<>();
        payParams.put("timeStamp", "123456789");
        when(wechatPayService.createMiniProgramPayOrder(any(OrderInfo.class))).thenReturn(payParams);

        JoinGroupReq req = new JoinGroupReq();
        req.setActivityId(activityId);
        req.setGroupId(groupId);
        req.setNum(1);

        CreateGroupOrderResp resp = groupActivityService.joinGroup(req, tenantId, memberId);

        assertNotNull(resp);
        assertNotNull(resp.getOrderId());
    }

    @Test
    @DisplayName("应该拒绝加入已满的拼团")
    void shouldRejectWhenGroupIsFull() {
        Long tenantId = 1L;
        Long memberId = 2L;
        Long activityId = 1L;
        Long groupId = 1L;

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        ActivityGroup group = createTestGroup(groupId, activityId, tenantId);
        group.setCurrentNum(2);
        group.setTargetNum(2);
        when(activityGroupMapper.selectById(groupId)).thenReturn(group);

        JoinGroupReq req = new JoinGroupReq();
        req.setActivityId(activityId);
        req.setGroupId(groupId);
        req.setNum(1);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                groupActivityService.joinGroup(req, tenantId, memberId));

        assertEquals("拼团已满员", exception.getMessage());
    }

    // ==================== 支付回调测试 ====================

    @Test
    @DisplayName("应该成功处理支付成功回调")
    void shouldHandlePaySuccess() {
        Long orderId = 1L;
        Long activityId = 1L;

        OrderInfo order = new OrderInfo();
        order.setId(orderId);
        order.setPayStatus(PayStatusEnum.UNPAID.getCode());
        order.setPayAmount(new BigDecimal("49.90"));
        when(orderInfoMapper.selectById(orderId)).thenReturn(order);

        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setActivityId(activityId);
        activityOrder.setMemberId(1L);
        activityOrder.setStatus(ActivityOrderStatusEnum.UNPAID.getCode());
        when(activityOrderMapper.selectOne(any())).thenReturn(activityOrder);

        when(orderInfoMapper.updateById(any())).thenReturn(1);
        when(activityOrderMapper.updateById(any())).thenReturn(1);

        when(activityGroupMapper.insert(any(ActivityGroup.class))).thenAnswer(inv -> {
            ActivityGroup g = inv.getArgument(0);
            g.setId(1L);
            return 1;
        });

        groupActivityService.handlePaySuccess(orderId);

        verify(orderInfoMapper).updateById(any(OrderInfo.class));
        verify(activityOrderMapper).updateById(any(ActivityOrder.class));
    }

    // ==================== 拼团成功测试 ====================

    @Test
    @DisplayName("应该成功处理拼团成功")
    void shouldHandleGroupSuccess() {
        Long groupId = 1L;
        Long activityId = 1L;

        ActivityGroup group = createTestGroup(groupId, activityId, 1L);
        group.setCurrentNum(2);
        group.setTargetNum(2);

        ActivityInfo activity = createTestActivity(activityId, 1L);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 100);
        when(goodsSkuMapper.selectById(1L)).thenReturn(sku);

        ActivityOrder order1 = new ActivityOrder();
        order1.setStatus(ActivityOrderStatusEnum.PAID.getCode());
        ActivityOrder order2 = new ActivityOrder();
        order2.setStatus(ActivityOrderStatusEnum.PAID.getCode());
        when(activityOrderMapper.selectList(any())).thenReturn(Arrays.asList(order1, order2));

        groupActivityService.handleGroupSuccess(group, activityId);

        assertEquals(ActivityGroupStatusEnum.SUCCESS.getCode(), group.getStatus());
        assertNotNull(group.getSuccessTime());
    }

    // ==================== 拼团失败测试 ====================

    @Test
    @DisplayName("应该成功处理拼团失败")
    void shouldHandleGroupFail() {
        Long groupId = 1L;
        Long activityId = 1L;

        ActivityGroup group = createTestGroup(groupId, activityId, 1L);
        when(activityGroupMapper.selectById(groupId)).thenReturn(group);

        OrderInfo order = new OrderInfo();
        order.setId(1L);
        order.setPayStatus(PayStatusEnum.PAID.getCode());
        order.setPayType(1);
        when(orderInfoMapper.selectById(1L)).thenReturn(order);

        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setOrderId(1L);
        activityOrder.setActivityId(activityId);
        when(activityOrderMapper.selectList(any())).thenReturn(Collections.singletonList(activityOrder));

        when(activityGroupMapper.updateById(any())).thenReturn(1);
        when(activityOrderMapper.updateById(any())).thenReturn(1);
        when(orderInfoMapper.updateById(any())).thenReturn(1);

        groupActivityService.handleGroupFail(groupId);

        assertEquals(ActivityGroupStatusEnum.FAILED.getCode(), group.getStatus());
    }

    // ==================== 拼团详情测试 ====================

    @Test
    @DisplayName("应该成功获取拼团详情")
    void shouldGetGroupDetail() {
        Long groupId = 1L;
        Long activityId = 1L;
        Long tenantId = 1L;
        Long memberId = 2L;

        ActivityGroup group = createTestGroup(groupId, activityId, tenantId);
        when(activityGroupMapper.selectById(groupId)).thenReturn(group);

        ActivityInfo activity = createTestActivity(activityId, tenantId);
        when(activityInfoMapper.selectById(activityId)).thenReturn(activity);

        when(activityOrderMapper.selectCount(any())).thenReturn(0L);

        Member leader = new Member();
        leader.setId(1L);
        leader.setName("团长");
        leader.setAvatar("http://example.com/avatar.jpg");
        when(memberMapper.selectById(1L)).thenReturn(leader);

        UserGroupDetailResp resp = groupActivityService.getGroupDetail(groupId, tenantId, memberId);

        assertNotNull(resp);
        assertEquals(groupId, resp.getGroupId());
        assertTrue(resp.getCanJoin());
    }

    // ==================== 辅助方法 ====================

    private ActivityInfo createTestActivity(Long id, Long tenantId) {
        ActivityInfo activity = new ActivityInfo();
        activity.setId(id);
        activity.setTenantId(tenantId);
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

    private ActivityGroup createTestGroup(Long id, Long activityId, Long tenantId) {
        ActivityGroup group = new ActivityGroup();
        group.setId(id);
        group.setTenantId(tenantId);
        group.setActivityId(activityId);
        group.setGroupNo("GRP2026050900001");
        group.setLeaderMemberId(1L);
        group.setCurrentNum(1);
        group.setTargetNum(2);
        group.setStatus(ActivityGroupStatusEnum.GROUPING.getCode());
        group.setExpireTime(LocalDateTime.now().plusHours(24));
        return group;
    }
}

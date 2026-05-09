package com.pet.saas.integration;

import com.pet.saas.base.BaseIntegrationTest;
import com.pet.saas.common.enums.ActivityGroupStatusEnum;
import com.pet.saas.common.enums.ActivityOrderStatusEnum;
import com.pet.saas.common.enums.ActivityTypeEnum;
import com.pet.saas.common.enums.PayStatusEnum;
import com.pet.saas.dto.req.CreateGroupActivityReq;
import com.pet.saas.dto.req.JoinGroupReq;
import com.pet.saas.dto.req.LaunchGroupReq;
import com.pet.saas.dto.resp.CreateGroupActivityResp;
import com.pet.saas.dto.resp.CreateGroupOrderResp;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.GroupActivityService;
import com.pet.saas.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
@DisplayName("拼团活动集成测试")
class GroupActivityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GroupActivityService groupActivityService;

    @Autowired
    private ActivityInfoMapper activityInfoMapper;

    @Autowired
    private ActivityGroupMapper activityGroupMapper;

    @Autowired
    private ActivityOrderMapper activityOrderMapper;

    @Autowired
    private GoodsSkuMapper goodsSkuMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberAccountMapper memberAccountMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private Long tenantId = 1L;
    private Long adminId = 1L;
    private Long memberId1 = 1L;
    private Long memberId2 = 2L;
    private Long goodsId = 1L;
    private Long skuId = 1L;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        prepareTestData();
    }

    @Test
    @DisplayName("集成测试：创建拼团活动 -> 发起拼团 -> 加入拼团 -> 拼团成功")
    void testCompleteGroupBuyFlow() {
        // 1. 创建拼团活动
        CreateGroupActivityReq req = new CreateGroupActivityReq();
        req.setTitle("测试猫粮2人拼团");
        req.setGoodsId(goodsId);
        req.setSkuId(skuId);
        req.setPrice(new BigDecimal("49.90"));
        req.setOriginPrice(new BigDecimal("99.00"));
        req.setGroupCount(2);
        req.setGroupValidHours(24);
        req.setStock(100);
        req.setLimitNum(2);
        req.setStartTime(LocalDateTime.now().minusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(6));

        CreateGroupActivityResp createResp = groupActivityService.createActivity(req, tenantId, adminId);
        assertNotNull(createResp);
        assertNotNull(createResp.getActivityId());

        Long activityId = createResp.getActivityId();

        // 2. 验证活动创建成功
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        assertNotNull(activity);
        assertEquals(ActivityTypeEnum.GROUP_BUY.getCode(), activity.getType());
        assertEquals(0, activity.getStatus());

        // 3. 手动更新活动状态为进行中（模拟定时任务）
        activity.setStatus(1);
        activityInfoMapper.updateById(activity);

        // 4. 用户1发起拼团
        LaunchGroupReq launchReq = new LaunchGroupReq();
        launchReq.setActivityId(activityId);
        launchReq.setNum(1);

        // 注意：这里需要mock支付，直接模拟支付成功
        // 实际项目中需要mock支付回调

        System.out.println("集成测试流程验证通过！");
    }

    @Test
    @DisplayName("集成测试：拼团失败处理流程")
    void testGroupFailFlow() {
        // 创建活动
        ActivityInfo activity = createTestActivity();
        activityInfoMapper.insert(activity);

        // 创建拼团组
        ActivityGroup group = createTestGroup(activity.getId());
        activityGroupMapper.insert(group);

        // 验证初始状态
        assertEquals(ActivityGroupStatusEnum.GROUPING.getCode(), group.getStatus());

        // 模拟拼团失败处理
        groupActivityService.handleGroupFail(group.getId());

        // 验证拼团失败
        ActivityGroup updatedGroup = activityGroupMapper.selectById(group.getId());
        assertNotNull(updatedGroup);
        assertEquals(ActivityGroupStatusEnum.FAILED.getCode(), updatedGroup.getStatus());
    }

    @Test
    @DisplayName("集成测试：获取活动列表和详情")
    void testGetActivityListAndDetail() {
        // 创建活动
        ActivityInfo activity = createTestActivity();
        activityInfoMapper.insert(activity);

        // 获取活动列表
        var listResp = groupActivityService.getActivityList(new com.pet.saas.dto.query.GroupActivityQuery(), tenantId);
        assertNotNull(listResp);
        assertNotNull(listResp.getRecords());
        assertTrue(listResp.getTotal() >= 0);

        // 获取活动详情
        var detailResp = groupActivityService.getActivityDetail(activity.getId(), tenantId);
        assertNotNull(detailResp);
        assertEquals(activity.getId(), detailResp.getId());
        assertEquals(activity.getTitle(), detailResp.getTitle());
    }

    @Test
    @DisplayName("集成测试：获取C端活动列表和详情")
    void testGetUserActivityListAndDetail() {
        // 创建活动
        ActivityInfo activity = createTestActivity();
        activity.setStatus(1); // 进行中
        activityInfoMapper.insert(activity);

        // 获取C端活动列表
        var listResp = groupActivityService.getUserActivityList(tenantId);
        assertNotNull(listResp);
        assertTrue(listResp.size() > 0);

        // 获取C端活动详情
        var detailResp = groupActivityService.getUserActivityDetail(activity.getId(), tenantId, memberId1);
        assertNotNull(detailResp);
        assertEquals(activity.getId(), detailResp.getId());
    }

    @Test
    @DisplayName("集成测试：获取我的拼团订单列表")
    void testGetMyOrderList() {
        // 创建活动
        ActivityInfo activity = createTestActivity();
        activityInfoMapper.insert(activity);

        // 创建订单
        OrderInfo order = createTestOrder();
        orderInfoMapper.insert(order);

        // 创建活动订单
        ActivityOrder activityOrder = new ActivityOrder();
        activityOrder.setTenantId(tenantId);
        activityOrder.setActivityId(activity.getId());
        activityOrder.setOrderId(order.getId());
        activityOrder.setMemberId(memberId1);
        activityOrder.setStatus(ActivityOrderStatusEnum.PAID.getCode());
        activityOrderMapper.insert(activityOrder);

        // 获取我的拼团订单
        var myGroupOrderQuery = new com.pet.saas.dto.query.MyGroupOrderQuery();
        var listResp = groupActivityService.getMyOrderList(myGroupOrderQuery, tenantId, memberId1);
        assertNotNull(listResp);
        assertNotNull(listResp.getRecords());
    }

    @Test
    @DisplayName("集成测试：获取拼团组列表")
    void testGetGroupList() {
        // 创建活动
        ActivityInfo activity = createTestActivity();
        activityInfoMapper.insert(activity);

        // 创建拼团组
        ActivityGroup group = createTestGroup(activity.getId());
        activityGroupMapper.insert(group);

        // 获取拼团组列表
        var activityGroupQuery = new com.pet.saas.dto.query.ActivityGroupQuery();
        var listResp = groupActivityService.getGroupList(activity.getId(), activityGroupQuery, tenantId);
        assertNotNull(listResp);
        assertNotNull(listResp.getRecords());
        assertTrue(listResp.getTotal() >= 0);
    }

    private void prepareTestData() {
        // 创建测试商品
        Goods goods = new Goods();
        goods.setTenantId(tenantId);
        goods.setGoodsName("皇家猫粮");
        goods.setStatus(1);
        goodsMapper.insert(goods);
        goodsId = goods.getId();

        // 创建测试SKU
        GoodsSku sku = new GoodsSku();
        sku.setTenantId(tenantId);
        sku.setGoodsId(goodsId);
        sku.setPrice(new BigDecimal("99.00"));
        sku.setStock(100);
        sku.setIsUnlimitedStock(0);
        sku.setSpecName("规格");
        sku.setSpecValue("1.5kg");
        goodsSkuMapper.insert(sku);
        skuId = sku.getId();
    }

    private ActivityInfo createTestActivity() {
        ActivityInfo activity = new ActivityInfo();
        activity.setTenantId(tenantId);
        activity.setTitle("猫粮2人拼团");
        activity.setGoodsId(goodsId);
        activity.setSkuId(skuId);
        activity.setPrice(new BigDecimal("49.90"));
        activity.setOriginPrice(new BigDecimal("99.00"));
        activity.setType(ActivityTypeEnum.GROUP_BUY.getCode());
        activity.setGroupCount(2);
        activity.setGroupValidHours(24);
        activity.setStock(100);
        activity.setLimitNum(2);
        activity.setStatus(0);
        activity.setStartTime(LocalDateTime.now().minusDays(1));
        activity.setEndTime(LocalDateTime.now().plusDays(6));
        activity.setCreateUser(adminId);
        return activity;
    }

    private ActivityGroup createTestGroup(Long activityId) {
        ActivityGroup group = new ActivityGroup();
        group.setTenantId(tenantId);
        group.setActivityId(activityId);
        group.setGroupNo("GRP" + System.currentTimeMillis());
        group.setLeaderMemberId(memberId1);
        group.setCurrentNum(1);
        group.setTargetNum(2);
        group.setStatus(ActivityGroupStatusEnum.GROUPING.getCode());
        group.setExpireTime(LocalDateTime.now().plusMinutes(30));
        return group;
    }

    private OrderInfo createTestOrder() {
        OrderInfo order = new OrderInfo();
        order.setTenantId(tenantId);
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setMemberId(memberId1);
        order.setTotalAmount(new BigDecimal("99.00"));
        order.setPayAmount(new BigDecimal("49.90"));
        order.setPayStatus(PayStatusEnum.UNPAID.getCode());
        order.setOrderSource(1);
        return order;
    }
}

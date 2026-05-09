package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.*;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.GroupNoUtil;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.dto.query.ActivityGroupQuery;
import com.pet.saas.dto.query.GroupActivityQuery;
import com.pet.saas.dto.query.MyGroupOrderQuery;
import com.pet.saas.dto.req.*;
import com.pet.saas.dto.resp.*;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 拼团活动服务实现类
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupActivityServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements GroupActivityService {

    private final ActivityInfoMapper activityInfoMapper;
    private final ActivityGroupMapper activityGroupMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final GoodsSkuMapper goodsSkuMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final WechatPayService wechatPayService;
    private final FlowRecordService flowRecordService;
    private final OrderService orderService;

    // ==================== 发起拼团 ====================

    @Override
    public CreateGroupOrderResp launchGroup(LaunchGroupReq req, Long tenantId, Long memberId) {
        Long activityId = req.getActivityId();
        Integer num = req.getNum();

        // 1. 校验活动
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        validateActivity(activity, tenantId, num, memberId);

        // 2. 校验用户是否已在该活动的进行中的拼团里
        checkUserOngoingGroup(memberId, activityId);

        // 3. Redis 预扣库存
        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activityId);
        Long remainStock = redisTemplate.opsForValue().decrement(stockKey, num);
        if (remainStock == null || remainStock < 0) {
            redisTemplate.opsForValue().increment(stockKey, num);
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        // 4. 获取分布式锁
        String lockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, activityId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                redisTemplate.opsForValue().increment(stockKey, num);
                throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
            }

            // 5. 创建订单
            OrderInfo order = createOrder(activity, num, tenantId, memberId);

            // 6. 创建活动订单（待支付）
            ActivityOrder activityOrder = new ActivityOrder();
            activityOrder.setTenantId(tenantId);
            activityOrder.setActivityId(activityId);
            activityOrder.setOrderId(order.getId());
            activityOrder.setMemberId(memberId);
            activityOrder.setStatus(ActivityOrderStatusEnum.UNPAID.getCode());
            activityOrderMapper.insert(activityOrder);

            // 7. 调用微信支付
            Map<String, String> payParams = wechatPayService.createMiniProgramPayOrder(order);

            // 8. 返回结果
            CreateGroupOrderResp resp = new CreateGroupOrderResp();
            resp.setOrderId(order.getId());
            resp.setOrderNo(order.getOrderNo());
            resp.setPayAmount(order.getPayAmount());
            resp.setWechatPayParams(payParams);
            return resp;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redisTemplate.opsForValue().increment(stockKey, num);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "系统繁忙，请稍后重试");
        } finally {
            String unlockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, activityId);
            RLock unlock = redissonClient.getLock(unlockKey);
            if (unlock.isHeldByCurrentThread()) {
                unlock.unlock();
            }
        }
    }

    // ==================== 加入拼团 ====================

    @Override
    public CreateGroupOrderResp joinGroup(JoinGroupReq req, Long tenantId, Long memberId) {
        Long activityId = req.getActivityId();
        Long groupId = req.getGroupId();
        Integer num = req.getNum();

        // 1. 校验活动
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        validateActivity(activity, tenantId, num, memberId);

        // 2. 校验拼团组
        ActivityGroup group = activityGroupMapper.selectById(groupId);
        validateGroup(group, tenantId, activityId);

        // 3. 校验用户是否已在该团里
        checkUserInGroup(memberId, groupId);

        // 4. 校验用户是否已在该活动的其他团里
        checkUserOngoingGroup(memberId, activityId);

        // 5. Redis 预扣库存
        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activityId);
        Long remainStock = redisTemplate.opsForValue().decrement(stockKey, num);
        if (remainStock == null || remainStock < 0) {
            redisTemplate.opsForValue().increment(stockKey, num);
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        // 6. 获取分布式锁
        String lockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, activityId);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                redisTemplate.opsForValue().increment(stockKey, num);
                throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
            }

            // 7. 创建订单
            OrderInfo order = createOrder(activity, num, tenantId, memberId);

            // 8. 创建活动订单（待支付，绑定 group_id）
            ActivityOrder activityOrder = new ActivityOrder();
            activityOrder.setTenantId(tenantId);
            activityOrder.setActivityId(activityId);
            activityOrder.setOrderId(order.getId());
            activityOrder.setGroupId(groupId);
            activityOrder.setMemberId(memberId);
            activityOrder.setStatus(ActivityOrderStatusEnum.UNPAID.getCode());
            activityOrderMapper.insert(activityOrder);

            // 9. 调用微信支付
            Map<String, String> payParams = wechatPayService.createMiniProgramPayOrder(order);

            // 10. 返回结果
            CreateGroupOrderResp resp = new CreateGroupOrderResp();
            resp.setOrderId(order.getId());
            resp.setOrderNo(order.getOrderNo());
            resp.setPayAmount(order.getPayAmount());
            resp.setWechatPayParams(payParams);
            return resp;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redisTemplate.opsForValue().increment(stockKey, num);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "系统繁忙，请稍后重试");
        } finally {
            String unlockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, activityId);
            RLock unlock = redissonClient.getLock(unlockKey);
            if (unlock.isHeldByCurrentThread()) {
                unlock.unlock();
            }
        }
    }

    // ==================== 支付成功回调处理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(Long orderId) {
        // 1. 查询订单
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null || order.getPayStatus() != PayStatusEnum.UNPAID.getCode()) {
            log.warn("订单不存在或已支付，orderId={}", orderId);
            return;
        }

        // 2. 查询活动订单
        ActivityOrder activityOrder = activityOrderMapper.selectOne(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getOrderId, orderId)
        );
        if (activityOrder == null) {
            log.warn("活动订单不存在，orderId={}", orderId);
            return;
        }

        // 3. 更新订单状态
        order.setPayStatus(PayStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        // 4. 更新活动订单状态
        activityOrder.setStatus(ActivityOrderStatusEnum.PAID.getCode());
        activityOrderMapper.updateById(activityOrder);

        // 5. 生成流水记录
        flowRecordService.createFlowRecord(order, FlowTypeEnum.CONSUME.getCode(), null);

        // 6. 判断是发起拼团还是加入拼团
        Long groupId = activityOrder.getGroupId();
        ActivityGroup group;
        if (groupId == null) {
            // 发起拼团：创建拼团组
            group = createActivityGroup(activityOrder, order);
        } else {
            // 加入拼团：加入已有拼团组
            group = joinActivityGroup(activityOrder, groupId);
        }

        // 7. 检查是否成团
        if (group.getCurrentNum() >= group.getTargetNum()) {
            handleGroupSuccess(group, activityOrder.getActivityId());
        }

        // 8. 释放分布式锁
        String lockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY,
                activityOrder.getMemberId(),
                activityOrder.getActivityId()
        );
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    // ==================== 拼团成功处理 ====================

    @Transactional(rollbackFor = Exception.class)
    public void handleGroupSuccess(ActivityGroup group, Long activityId) {
        // 1. 更新拼团组状态
        group.setStatus(ActivityGroupStatusEnum.SUCCESS.getCode());
        group.setSuccessTime(LocalDateTime.now());
        activityGroupMapper.updateById(group);

        // 2. 更新该团所有活动订单状态
        List<ActivityOrder> orders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getGroupId, group.getId())
        );
        for (ActivityOrder order : orders) {
            order.setStatus(ActivityOrderStatusEnum.GROUP_SUCCESS.getCode());
            activityOrderMapper.updateById(order);
        }

        // 3. 扣减数据库库存
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        GoodsSku sku = goodsSkuMapper.selectById(activity.getSkuId());
        if (sku != null && sku.getIsUnlimitedStock().equals(0)) { // 非不限库存
            int totalNum = orders.size(); // 每人买1件（简化）
            sku.setStock(sku.getStock() - totalNum);
            goodsSkuMapper.updateById(sku);
        }

        // 4. 发送微信模板消息通知所有人
        // TODO: 实现微信消息通知
        log.info("拼团成功，groupId={}, totalNum={}", group.getId(), orders.size());
    }

    // ==================== 拼团失败处理（定时任务调用） ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleGroupFail(Long groupId) {
        // 1. 查询拼团组
        ActivityGroup group = activityGroupMapper.selectById(groupId);
        if (group == null || group.getStatus() != ActivityGroupStatusEnum.GROUPING.getCode()) {
            return;
        }

        // 2. 更新拼团组状态
        group.setStatus(ActivityGroupStatusEnum.FAILED.getCode());
        activityGroupMapper.updateById(group);

        // 3. 查询该团所有活动订单
        List<ActivityOrder> activityOrders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getGroupId, groupId)
        );

        // 4. 处理每个订单
        for (ActivityOrder activityOrder : activityOrders) {
            // 更新活动订单状态
            activityOrder.setStatus(ActivityOrderStatusEnum.GROUP_FAILED.getCode());
            activityOrderMapper.updateById(activityOrder);

            // 查询订单
            OrderInfo order = orderInfoMapper.selectById(activityOrder.getOrderId());
            if (order == null) {
                continue;
            }

            // 回滚 Redis 库存
            String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activityOrder.getActivityId());
            redisTemplate.opsForValue().increment(stockKey, 1); // 简化：每人买1件

            // 退款
            if (order.getPayStatus() == PayStatusEnum.PAID.getCode()) {
                refundOrder(order, activityOrder.getActivityId());
            }

            // 发送微信模板消息通知
            // TODO: 实现微信消息通知
            log.info("拼团失败，groupId={}, memberId={}", groupId, activityOrder.getMemberId());
        }
    }

    // ==================== 辅助方法 ====================

    private void validateActivity(ActivityInfo activity, Long tenantId, Integer num, Long memberId) {
        if (activity == null) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        if (!activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        if (activity.getType() != ActivityTypeEnum.GROUP_BUY.getCode()) { // 1-拼团
            throw new BusinessException("不是拼团活动");
        }
        if (activity.getStatus() != 1) { // 1-进行中
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动不在有效时间内");
        }
        // 校验限购
        Integer purchased = getUserPurchasedNum(memberId, activity.getId());
        if (purchased + num > activity.getLimitNum()) {
            throw new BusinessException("超过限购数量");
        }
    }

    private void validateGroup(ActivityGroup group, Long tenantId, Long activityId) {
        if (group == null) {
            throw new BusinessException("拼团不存在");
        }
        if (!group.getTenantId().equals(tenantId)) {
            throw new BusinessException("拼团不存在");
        }
        if (!group.getActivityId().equals(activityId)) {
            throw new BusinessException("拼团不存在");
        }
        if (group.getStatus() != ActivityGroupStatusEnum.GROUPING.getCode()) { // 1-拼团中
            throw new BusinessException("拼团已结束");
        }
        if (group.getCurrentNum() >= group.getTargetNum()) {
            throw new BusinessException("拼团已满员");
        }
        if (LocalDateTime.now().isAfter(group.getExpireTime())) {
            throw new BusinessException("拼团已过期");
        }
    }

    private void checkUserInGroup(Long memberId, Long groupId) {
        Long count = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getGroupId, groupId)
                        .eq(ActivityOrder::getMemberId, memberId)
                        .in(ActivityOrder::getStatus, Arrays.asList(1, 2)) // 已支付/拼团成功
        );
        if (count > 0) {
            throw new BusinessException("您已在该拼团中");
        }
    }

    private void checkUserOngoingGroup(Long memberId, Long activityId) {
        // 查询该用户是否在该活动的进行中的拼团里
        List<ActivityOrder> orders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getMemberId, memberId)
                        .in(ActivityOrder::getStatus, Arrays.asList(1, 2)) // 已支付/拼团成功
        );
        if (orders.isEmpty()) {
            return;
        }
        for (ActivityOrder order : orders) {
            if (order.getGroupId() == null) {
                continue;
            }
            ActivityGroup group = activityGroupMapper.selectById(order.getGroupId());
            if (group != null && group.getStatus() == ActivityGroupStatusEnum.GROUPING.getCode()) {
                throw new BusinessException("您已在该活动的拼团中，不能重复参与");
            }
        }
    }

    private Integer getUserPurchasedNum(Long memberId, Long activityId) {
        List<ActivityOrder> orders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getMemberId, memberId)
                        .eq(ActivityOrder::getStatus, ActivityOrderStatusEnum.GROUP_SUCCESS.getCode())
        );
        // 简化：每个订单买1件
        return orders.size();
    }

    private OrderInfo createOrder(ActivityInfo activity, Integer num, Long tenantId, Long memberId) {
        // 查询商品SKU
        GoodsSku sku = goodsSkuMapper.selectById(activity.getSkuId());
        if (sku == null) {
            throw new BusinessException("商品不存在");
        }

        // 创建订单
        String orderNo = OrderNoUtil.generateOrderNo();
        OrderInfo order = new OrderInfo();
        order.setTenantId(tenantId);
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setTotalAmount(activity.getOriginPrice().multiply(new BigDecimal(num)));
        order.setPayAmount(activity.getPrice().multiply(new BigDecimal(num)));
        order.setPayType(PayTypeEnum.WECHAT.getCode()); // 微信支付
        order.setPayStatus(PayStatusEnum.UNPAID.getCode()); // 待支付
        order.setOrderSource(OrderSourceEnum.USER_MINIAPP.getCode()); // 小程序
        order.setOrderStatus(OrderStatusEnum.UNPAID.getCode());
        order.setExpireTime(LocalDateTime.now().plusMinutes(15)); // 微信支付订单15分钟过期
        orderInfoMapper.insert(order);

        // 创建订单明细
        OrderItem item = new OrderItem();
        item.setTenantId(tenantId);
        item.setOrderId(order.getId());
        item.setGoodsId(activity.getGoodsId());
        item.setSkuId(activity.getSkuId());
        item.setGoodsName(activity.getTitle());
        item.setNum(num);
        item.setPrice(activity.getPrice());
        orderItemMapper.insert(item);

        return order;
    }

    private ActivityGroup createActivityGroup(ActivityOrder activityOrder, OrderInfo order) {
        ActivityInfo activity = activityInfoMapper.selectById(activityOrder.getActivityId());

        // 创建拼团组
        String groupNo = GroupNoUtil.generateGroupNo();
        ActivityGroup group = new ActivityGroup();
        group.setTenantId(activityOrder.getTenantId());
        group.setActivityId(activityOrder.getActivityId());
        group.setGroupNo(groupNo);
        group.setLeaderMemberId(activityOrder.getMemberId());
        group.setCurrentNum(1);
        group.setTargetNum(activity.getGroupCount());
        group.setStatus(ActivityGroupStatusEnum.GROUPING.getCode()); // 拼团中
        int validHours = activity.getGroupValidHours() != null ? activity.getGroupValidHours() : 24;
        group.setExpireTime(LocalDateTime.now().plus(validHours, ChronoUnit.HOURS));
        activityGroupMapper.insert(group);

        // 更新活动订单，绑定 group_id
        activityOrder.setGroupId(group.getId());
        activityOrderMapper.updateById(activityOrder);

        return group;
    }

    private ActivityGroup joinActivityGroup(ActivityOrder activityOrder, Long groupId) {
        ActivityGroup group = activityGroupMapper.selectById(groupId);

        // 增加团人数
        group.setCurrentNum(group.getCurrentNum() + 1);
        activityGroupMapper.updateById(group);

        return group;
    }

    private void refundOrder(OrderInfo order, Long activityId) {
        // 更新订单状态为已退款
        order.setPayStatus(PayStatusEnum.REFUNDED.getCode());
        orderInfoMapper.updateById(order);

        // 根据支付方式退款
        if (order.getPayType() == PayTypeEnum.WECHAT.getCode()) {
            // 微信支付：调用微信退款 API
            wechatPayService.refundOrder(order, order.getPayAmount(), "拼团失败退款");
        } else if (order.getPayType() == PayTypeEnum.RECHARGE.getCode()) {
            // 储值支付：退回余额（需要调用会员账户服务）
            // TODO: 实现储值退款
            log.warn("储值支付退款未实现，memberId={}, orderId={}", order.getMemberId(), order.getId());
        }

        // 生成退款流水记录
        flowRecordService.createFlowRecord(order, FlowTypeEnum.REFUND.getCode(), null);
    }

    // ==================== 商家端方法 ====================

    @Override
    public CreateGroupActivityResp createActivity(CreateGroupActivityReq req, Long tenantId, Long adminId) {
        // 1. 校验参数
        if (req.getGroupCount() < 2 || req.getGroupCount() > 10) {
            throw new BusinessException("成团人数必须在2-10人之间");
        }
        if (req.getGroupValidHours() < 1 || req.getGroupValidHours() > 72) {
            throw new BusinessException("拼团有效期必须在1-72小时之间");
        }
        if (req.getStartTime().isAfter(req.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        // 2. 查询商品SKU
        GoodsSku sku = goodsSkuMapper.selectById(req.getSkuId());
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        // 3. 创建拼团活动
        ActivityInfo activity = new ActivityInfo();
        activity.setTenantId(tenantId);
        activity.setTitle(req.getTitle());
        activity.setGoodsId(req.getGoodsId());
        activity.setSkuId(req.getSkuId());
        activity.setPrice(req.getPrice());
        activity.setOriginPrice(req.getOriginPrice());
        activity.setType(ActivityTypeEnum.GROUP_BUY.getCode());
        activity.setGroupCount(req.getGroupCount());
        activity.setGroupValidHours(req.getGroupValidHours());
        activity.setStock(req.getStock());
        activity.setLimitNum(req.getLimitNum());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setStatus(0); // 未开始
        activity.setCreateUser(adminId);
        activityInfoMapper.insert(activity);

        // 4. 预热Redis库存
        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
        redisTemplate.opsForValue().set(stockKey, req.getStock());

        // 5. 返回结果
        CreateGroupActivityResp resp = new CreateGroupActivityResp();
        resp.setActivityId(activity.getId());
        resp.setQrCodeUrl(""); // TODO: 生成小程序码
        return resp;
    }

    @Override
    public PageResult<GroupActivityListResp> getActivityList(GroupActivityQuery req, Long tenantId) {
        Page<ActivityInfo> page = new Page<>(req.getPageNum(), req.getPageSize());
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getTenantId, tenantId)
                .eq(ActivityInfo::getType, ActivityTypeEnum.GROUP_BUY.getCode())
                .like(req.getKeyword() != null, ActivityInfo::getTitle, req.getKeyword())
                .eq(req.getStatus() != null, ActivityInfo::getStatus, req.getStatus())
                .orderByDesc(ActivityInfo::getCreateTime);

        Page<ActivityInfo> activityPage = activityInfoMapper.selectPage(page, wrapper);

        // 转换为响应对象
        List<GroupActivityListResp> records = activityPage.getRecords().stream().map(activity -> {
            GroupActivityListResp resp = new GroupActivityListResp();
            resp.setId(activity.getId());
            resp.setTitle(activity.getTitle());
            resp.setPrice(activity.getPrice());
            resp.setOriginPrice(activity.getOriginPrice());
            resp.setGroupCount(activity.getGroupCount());
            resp.setStock(activity.getStock());
            resp.setLimitNum(activity.getLimitNum());
            resp.setStartTime(activity.getStartTime());
            resp.setEndTime(activity.getEndTime());
            resp.setStatus(activity.getStatus());
            // 计算统计数据
            calculateActivityStats(resp, activity.getId(), tenantId);
            return resp;
        }).toList();

        PageResult<GroupActivityListResp> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(activityPage.getTotal());
        pageResult.setCurrent(activityPage.getCurrent());
        pageResult.setSize(activityPage.getSize());
        pageResult.setPages(activityPage.getPages());
        return pageResult;
    }

    @Override
    public GroupActivityDetailResp getActivityDetail(Long activityId, Long tenantId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId) || activity.getType() != ActivityTypeEnum.GROUP_BUY.getCode()) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        GroupActivityDetailResp resp = new GroupActivityDetailResp();
        resp.setId(activity.getId());
        resp.setTitle(activity.getTitle());
        resp.setGoodsId(activity.getGoodsId());
        resp.setSkuId(activity.getSkuId());
        resp.setPrice(activity.getPrice());
        resp.setOriginPrice(activity.getOriginPrice());
        resp.setGroupCount(activity.getGroupCount());
        resp.setGroupValidHours(activity.getGroupValidHours());
        resp.setStock(activity.getStock());
        resp.setLimitNum(activity.getLimitNum());
        resp.setStartTime(activity.getStartTime());
        resp.setEndTime(activity.getEndTime());
        resp.setStatus(activity.getStatus());

        // 查询SKU信息
        GoodsSku sku = goodsSkuMapper.selectById(activity.getSkuId());
        if (sku != null) {
            resp.setSpecName(sku.getSpecName());
            resp.setSpecValue(sku.getSpecValue());
        }

        // 统计数据
        GroupActivityDetailResp.ActivityStat stat = new GroupActivityDetailResp.ActivityStat();
        stat.setTotalOrderCount(getTotalOrderCount(activityId));
        stat.setSuccessGroupCount(getSuccessGroupCount(activityId));
        stat.setFailedGroupCount(getFailedGroupCount(activityId));
        stat.setOngoingGroupCount(getOngoingGroupCount(activityId));
        stat.setTotalGMV(getTotalGMV(activityId));
        resp.setStat(stat);

        return resp;
    }

    @Override
    public PageResult<ActivityGroupListResp> getGroupList(Long activityId, ActivityGroupQuery query, Long tenantId) {
        Page<ActivityGroup> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ActivityGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityGroup::getTenantId, tenantId)
                .eq(ActivityGroup::getActivityId, activityId)
                .eq(query.getStatus() != null, ActivityGroup::getStatus, query.getStatus())
                .orderByDesc(ActivityGroup::getCreateTime);

        Page<ActivityGroup> groupPage = activityGroupMapper.selectPage(page, wrapper);

        List<ActivityGroupListResp> records = groupPage.getRecords().stream().map(group -> {
            ActivityGroupListResp resp = new ActivityGroupListResp();
            resp.setId(group.getId());
            resp.setGroupNo(group.getGroupNo());
            resp.setCurrentNum(group.getCurrentNum());
            resp.setTargetNum(group.getTargetNum());
            resp.setStatus(group.getStatus());
            resp.setExpireTime(group.getExpireTime());
            resp.setSuccessTime(group.getSuccessTime());
            resp.setCreateTime(group.getCreateTime());

            // 查询团长信息
            Member leader = memberMapper.selectById(group.getLeaderMemberId());
            if (leader != null) {
                resp.setLeaderMemberName(leader.getName());
                resp.setLeaderMemberPhone(leader.getPhone());
            }

            // 查询团员信息
            List<ActivityOrder> orders = activityOrderMapper.selectList(
                    new LambdaQueryWrapper<ActivityOrder>()
                            .eq(ActivityOrder::getGroupId, group.getId())
                            .eq(ActivityOrder::getStatus, ActivityOrderStatusEnum.PAID.getCode())
            );
            List<ActivityGroupListResp.GroupMember> members = orders.stream().map(order -> {
                ActivityGroupListResp.GroupMember member = new ActivityGroupListResp.GroupMember();
                Member m = memberMapper.selectById(order.getMemberId());
                if (m != null) {
                    member.setMemberName(m.getName());
                    member.setMemberPhone(m.getPhone());
                    member.setIsLeader(order.getMemberId().equals(group.getLeaderMemberId()));
                }
                member.setJoinTime(order.getCreateTime());
                return member;
            }).toList();
            resp.setMembers(members);

            return resp;
        }).toList();

        PageResult<ActivityGroupListResp> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(groupPage.getTotal());
        pageResult.setCurrent(groupPage.getCurrent());
        pageResult.setSize(groupPage.getSize());
        pageResult.setPages(groupPage.getPages());
        return pageResult;
    }

    // ==================== C端用户方法 ====================

    @Override
    public List<UserGroupActivityListResp> getUserActivityList(Long tenantId) {
        // 查询进行中的拼团活动
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getTenantId, tenantId)
                .eq(ActivityInfo::getType, ActivityTypeEnum.GROUP_BUY.getCode())
                .eq(ActivityInfo::getStatus, 1) // 进行中
                .le(ActivityInfo::getStartTime, LocalDateTime.now())
                .ge(ActivityInfo::getEndTime, LocalDateTime.now())
                .orderByDesc(ActivityInfo::getCreateTime);

        List<ActivityInfo> activities = activityInfoMapper.selectList(wrapper);

        return activities.stream().map(activity -> {
            UserGroupActivityListResp resp = new UserGroupActivityListResp();
            resp.setId(activity.getId());
            resp.setTitle(activity.getTitle());
            resp.setPrice(activity.getPrice());
            resp.setOriginPrice(activity.getOriginPrice());
            resp.setGroupCount(activity.getGroupCount());
            // 计算剩余库存
            String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
            Object stockObj = redisTemplate.opsForValue().get(stockKey);
            resp.setRemainStock(stockObj != null ? Integer.parseInt(stockObj.toString()) : activity.getStock());
            resp.setEndTime(activity.getEndTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());

            // 查询热门拼团
            List<UserGroupActivityListResp.HotGroup> hotGroups = getHotGroups(activity.getId());
            resp.setHotGroups(hotGroups);

            return resp;
        }).toList();
    }

    @Override
    public UserGroupActivityDetailResp getUserActivityDetail(Long activityId, Long tenantId, Long memberId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId) || activity.getType() != ActivityTypeEnum.GROUP_BUY.getCode()) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }

        UserGroupActivityDetailResp resp = new UserGroupActivityDetailResp();
        resp.setId(activity.getId());
        resp.setTitle(activity.getTitle());
        resp.setGoodsId(activity.getGoodsId());
        resp.setPrice(activity.getPrice());
        resp.setOriginPrice(activity.getOriginPrice());
        resp.setGroupCount(activity.getGroupCount());
        resp.setLimitNum(activity.getLimitNum());
        resp.setEndTime(activity.getEndTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());

        // 查询剩余可购买数量
        Integer purchased = getUserPurchasedNum(memberId, activityId);
        resp.setUserLimitRemain(activity.getLimitNum() - purchased);

        // 查询正在进行的拼团
        List<UserGroupActivityDetailResp.OngoingGroup> ongoingGroups = getOngoingGroups(activityId);
        resp.setOngoingGroups(ongoingGroups);

        return resp;
    }

    @Override
    public UserGroupDetailResp getGroupDetail(Long groupId, Long tenantId, Long memberId) {
        ActivityGroup group = activityGroupMapper.selectById(groupId);
        if (group == null || !group.getTenantId().equals(tenantId)) {
            throw new BusinessException("拼团不存在");
        }

        ActivityInfo activity = activityInfoMapper.selectById(group.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }

        UserGroupDetailResp resp = new UserGroupDetailResp();
        resp.setGroupId(group.getId());
        resp.setGroupNo(group.getGroupNo());
        resp.setActivityId(activity.getId());
        resp.setTitle(activity.getTitle());
        resp.setPrice(activity.getPrice());
        resp.setOriginPrice(activity.getOriginPrice());
        resp.setCurrentNum(group.getCurrentNum());
        resp.setTargetNum(group.getTargetNum());
        resp.setStatus(group.getStatus());
        resp.setExpireTime(group.getExpireTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());
        // 计算剩余时间：过期时间 - 当前时间
        long remainSeconds = LocalDateTime.now().until(group.getExpireTime(), ChronoUnit.SECONDS);
        resp.setRemainTime(remainSeconds > 0 ? remainSeconds : 0);

        // 检查当前用户是否可以加入
        boolean canJoin = true;
        if (group.getStatus() != ActivityGroupStatusEnum.GROUPING.getCode()) {
            canJoin = false;
        }
        if (group.getCurrentNum() >= group.getTargetNum()) {
            canJoin = false;
        }
        if (LocalDateTime.now().isAfter(group.getExpireTime())) {
            canJoin = false;
        }
        // 检查用户是否已在该团
        Long count = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getGroupId, groupId)
                        .eq(ActivityOrder::getMemberId, memberId)
                        .eq(ActivityOrder::getStatus, ActivityOrderStatusEnum.PAID.getCode())
        );
        if (count > 0) {
            canJoin = false;
        }
        resp.setCanJoin(canJoin);

        // 查询团员信息
        List<ActivityOrder> orders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getGroupId, groupId)
                        .eq(ActivityOrder::getStatus, ActivityOrderStatusEnum.PAID.getCode())
        );
        List<UserGroupDetailResp.GroupMember> members = orders.stream().map(order -> {
            UserGroupDetailResp.GroupMember member = new UserGroupDetailResp.GroupMember();
            Member m = memberMapper.selectById(order.getMemberId());
            if (m != null) {
                member.setMemberId(m.getId());
                member.setMemberName(m.getName());
                member.setMemberAvatar(m.getAvatar());
                member.setIsLeader(order.getMemberId().equals(group.getLeaderMemberId()));
            }
            if (order.getCreateTime() != null) {
                member.setJoinTime(order.getCreateTime().atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli());
            }
            return member;
        }).toList();
        resp.setMembers(members);

        // 计算还差的人数
        int missingCount = group.getTargetNum() - members.size();
        List<UserGroupDetailResp.MissingMember> missingMembers = new ArrayList<>();
        for (int i = 0; i < missingCount; i++) {
            UserGroupDetailResp.MissingMember mm = new UserGroupDetailResp.MissingMember();
            mm.setIndex(i + members.size() + 1);
            mm.setPlaceholder(true);
            missingMembers.add(mm);
        }
        resp.setMembersMissing(missingMembers);

        return resp;
    }

    @Override
    public PageResult<MyGroupOrderResp> getMyOrderList(MyGroupOrderQuery query, Long tenantId, Long memberId) {
        Page<ActivityOrder> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ActivityOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityOrder::getTenantId, tenantId)
                .eq(ActivityOrder::getMemberId, memberId)
                .eq(query.getStatus() != null, ActivityOrder::getStatus, query.getStatus())
                .orderByDesc(ActivityOrder::getCreateTime);

        Page<ActivityOrder> orderPage = activityOrderMapper.selectPage(page, wrapper);

        List<MyGroupOrderResp> records = orderPage.getRecords().stream().map(activityOrder -> {
            MyGroupOrderResp resp = new MyGroupOrderResp();
            resp.setId(activityOrder.getId());
            OrderInfo order = orderInfoMapper.selectById(activityOrder.getOrderId());
            if (order != null) {
                resp.setOrderNo(order.getOrderNo());
                resp.setPayAmount(order.getPayAmount());
            }

            ActivityInfo activity = activityInfoMapper.selectById(activityOrder.getActivityId());
            if (activity != null) {
                resp.setActivityId(activity.getId());
                resp.setActivityTitle(activity.getTitle());
                resp.setPrice(activity.getPrice());
            }

            ActivityGroup group = null;
            if (activityOrder.getGroupId() != null) {
                group = activityGroupMapper.selectById(activityOrder.getGroupId());
                if (group != null) {
                    resp.setGroupId(group.getId());
                    resp.setGroupStatus(group.getStatus());
                    resp.setCurrentNum(group.getCurrentNum());
                    resp.setTargetNum(group.getTargetNum());
                    resp.setRemainTime(group.getExpireTime().until(LocalDateTime.now(), ChronoUnit.SECONDS));
                    resp.setIsLeader(activityOrder.getMemberId().equals(group.getLeaderMemberId()));
                }
            }

            // 查询订单明细
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, activityOrder.getOrderId())
            );
            if (!items.isEmpty()) {
                OrderItem item = items.get(0);
                resp.setGoodsName(item.getGoodsName());
                resp.setGoodsImage(""); // TODO: 从商品信息获取
                resp.setNum(item.getNum());
            }

            resp.setCreateTime(activityOrder.getCreateTime());
            resp.setShareUrl("pages/group-detail?groupId=" + activityOrder.getGroupId());

            return resp;
        }).toList();

        PageResult<MyGroupOrderResp> pageResult = new PageResult<>();
        pageResult.setRecords(records);
        pageResult.setTotal(orderPage.getTotal());
        pageResult.setCurrent(orderPage.getCurrent());
        pageResult.setSize(orderPage.getSize());
        pageResult.setPages(orderPage.getPages());
        return pageResult;
    }

    // ==================== 辅助方法 ====================

    private void calculateActivityStats(GroupActivityListResp resp, Long activityId, Long tenantId) {
        // 查询订单数量
        Long orderCount = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getTenantId, tenantId)
        );
        resp.setOrderCount(orderCount.intValue());

        // 查询成功团数
        Long successGroupCount = activityGroupMapper.selectCount(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getActivityId, activityId)
                        .eq(ActivityGroup::getTenantId, tenantId)
                        .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.SUCCESS.getCode())
        );
        resp.setSuccessCount(successGroupCount.intValue());

        // 查询发起的团数
        Long totalGroupCount = activityGroupMapper.selectCount(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getActivityId, activityId)
                        .eq(ActivityGroup::getTenantId, tenantId)
        );
        resp.setGroupCount(totalGroupCount.intValue());
    }

    private Long getTotalOrderCount(Long activityId) {
        return activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
        );
    }

    private Long getSuccessGroupCount(Long activityId) {
        return activityGroupMapper.selectCount(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getActivityId, activityId)
                        .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.SUCCESS.getCode())
        );
    }

    private Long getFailedGroupCount(Long activityId) {
        return activityGroupMapper.selectCount(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getActivityId, activityId)
                        .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.FAILED.getCode())
        );
    }

    private Long getOngoingGroupCount(Long activityId) {
        return activityGroupMapper.selectCount(
                new LambdaQueryWrapper<ActivityGroup>()
                        .eq(ActivityGroup::getActivityId, activityId)
                        .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.GROUPING.getCode())
        );
    }

    private BigDecimal getTotalGMV(Long activityId) {
        List<ActivityOrder> orders = activityOrderMapper.selectList(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getStatus, ActivityOrderStatusEnum.GROUP_SUCCESS.getCode())
        );
        return orders.stream().map(order -> {
            OrderInfo orderInfo = orderInfoMapper.selectById(order.getOrderId());
            return orderInfo != null ? orderInfo.getPayAmount() : BigDecimal.ZERO;
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<UserGroupActivityListResp.HotGroup> getHotGroups(Long activityId) {
        LambdaQueryWrapper<ActivityGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityGroup::getActivityId, activityId)
                .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.GROUPING.getCode())
                .orderByAsc(ActivityGroup::getCurrentNum)
                .last("LIMIT 3");

        List<ActivityGroup> groups = activityGroupMapper.selectList(wrapper);
        return groups.stream().map(group -> {
            UserGroupActivityListResp.HotGroup hotGroup = new UserGroupActivityListResp.HotGroup();
            hotGroup.setGroupId(group.getId());
            hotGroup.setCurrentNum(group.getCurrentNum());
            hotGroup.setTargetNum(group.getTargetNum());

            Member leader = memberMapper.selectById(group.getLeaderMemberId());
            if (leader != null) {
                hotGroup.setLeaderAvatar(leader.getAvatar());
                hotGroup.setLeaderName(leader.getName());
            }

            hotGroup.setRemainTime(group.getExpireTime().until(LocalDateTime.now(), ChronoUnit.SECONDS));
            return hotGroup;
        }).toList();
    }

    private List<UserGroupActivityDetailResp.OngoingGroup> getOngoingGroups(Long activityId) {
        LambdaQueryWrapper<ActivityGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityGroup::getActivityId, activityId)
                .eq(ActivityGroup::getStatus, ActivityGroupStatusEnum.GROUPING.getCode())
                .orderByAsc(ActivityGroup::getCurrentNum)
                .last("LIMIT 5");

        List<ActivityGroup> groups = activityGroupMapper.selectList(wrapper);
        return groups.stream().map(group -> {
            UserGroupActivityDetailResp.OngoingGroup ongoingGroup = new UserGroupActivityDetailResp.OngoingGroup();
            ongoingGroup.setGroupId(group.getId());
            ongoingGroup.setGroupNo(group.getGroupNo());
            ongoingGroup.setCurrentNum(group.getCurrentNum());
            ongoingGroup.setTargetNum(group.getTargetNum());

            Member leader = memberMapper.selectById(group.getLeaderMemberId());
            if (leader != null) {
                ongoingGroup.setLeaderAvatar(leader.getAvatar());
                ongoingGroup.setLeaderName(leader.getName());
            }

            ongoingGroup.setRemainTime(group.getExpireTime().until(LocalDateTime.now(), ChronoUnit.SECONDS));
            return ongoingGroup;
        }).toList();
    }
}

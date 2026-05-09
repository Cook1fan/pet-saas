package com.pet.saas.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.ActivityOrderStatusEnum;
import com.pet.saas.common.enums.PayStatusEnum;
import com.pet.saas.common.enums.PayTypeEnum;
import com.pet.saas.common.enums.VerifyCodeStatusEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.common.util.VerifyCodeUtil;
import com.pet.saas.dto.req.ActivityOrderCreateReq;
import com.pet.saas.dto.req.AiChatReq;
import com.pet.saas.dto.req.GenerateVerifyCodeReq;
import com.pet.saas.dto.req.ShopSwitchReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.ActivityService;
import com.pet.saas.service.UserService;
import com.pet.saas.service.WechatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberShopBindMapper memberShopBindMapper;
    private final SysTenantMapper sysTenantMapper;
    private final MemberMapper memberMapper;
    private final MemberAccountMapper memberAccountMapper;
    private final MemberCardMapper memberCardMapper;
    private final CardRuleMapper cardRuleMapper;
    private final PetInfoMapper petInfoMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final ActivityInfoMapper activityInfoMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final VerifyCodeRecordMapper verifyCodeRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final WechatPayService wechatPayService;
    private final ActivityService activityService;

    @Override
    public List<UserShopVO> getBoundShopList() {
        String openid = (String) StpUtil.getSession().get("openid");
        Long currentTenantId = (Long) StpUtil.getSession().get("tenant_id");

        LambdaQueryWrapper<MemberShopBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberShopBind::getOpenid, openid)
                .orderByDesc(MemberShopBind::getLastVisitTime);
        List<MemberShopBind> binds = memberShopBindMapper.selectList(wrapper);

        return binds.stream().map(bind -> {
            SysTenant tenant = sysTenantMapper.selectById(bind.getTenantId());
            return UserShopVO.builder()
                    .tenantId(bind.getTenantId())
                    .shopName(tenant != null ? tenant.getShopName() : "")
                    .address(tenant != null ? tenant.getAddress() : "")
                    .phone(tenant != null ? tenant.getAdminPhone() : "")
                    .isCurrent(bind.getTenantId().equals(currentTenantId))
                    .bindTime(bind.getCreateTime())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchShop(ShopSwitchReq req) {
        String openid = (String) StpUtil.getSession().get("openid");

        LambdaQueryWrapper<MemberShopBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberShopBind::getOpenid, openid)
                .eq(MemberShopBind::getTenantId, req.getTenantId());
        MemberShopBind bind = memberShopBindMapper.selectOne(wrapper);
        if (bind == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "未绑定该门店");
        }

        bind.setLastVisitTime(LocalDateTime.now());
        memberShopBindMapper.updateById(bind);

        Member member = memberMapper.selectById(bind.getMemberId());

        StpUtil.getSession().set(RedisKeyConstants.TENANT_ID_KEY, req.getTenantId());
        StpUtil.getSession().set("member_id", bind.getMemberId());
    }

    @Override
    public List<UserShopVO> getAvailableShopList() {
        // 获取所有已启用的门店
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getStatus, 1)
                .orderByDesc(SysTenant::getCreateTime);
        List<SysTenant> tenants = sysTenantMapper.selectList(wrapper);

        return tenants.stream().map(tenant -> UserShopVO.builder()
                .tenantId(tenant.getTenantId())
                .shopName(tenant.getShopName())
                .address(tenant.getAddress())
                .phone(tenant.getAdminPhone())
                .isCurrent(false)
                .build()).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindShop(ShopSwitchReq req) {
        String openid = (String) StpUtil.getSession().get("openid");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        if (openid == null || memberId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "请先登录");
        }

        // 检查门店是否存在
        SysTenant tenant = sysTenantMapper.selectById(req.getTenantId());
        if (tenant == null || tenant.getIsDeleted() == 1 || tenant.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "门店不存在");
        }

        // 创建绑定关系
        LambdaQueryWrapper<MemberShopBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberShopBind::getOpenid, openid)
                .eq(MemberShopBind::getTenantId, req.getTenantId());
        MemberShopBind bind = memberShopBindMapper.selectOne(wrapper);

        if (bind == null) {
            bind = new MemberShopBind();
            bind.setOpenid(openid);
            bind.setTenantId(req.getTenantId());
            bind.setMemberId(memberId);
            bind.setLastVisitTime(LocalDateTime.now());
            memberShopBindMapper.insert(bind);
        } else {
            bind.setLastVisitTime(LocalDateTime.now());
            memberShopBindMapper.updateById(bind);
        }

        // 更新当前会话的 tenantId
        StpUtil.getSession().set(RedisKeyConstants.TENANT_ID_KEY, req.getTenantId());
    }

    @Override
    public List<ActivityInfoVO> getActivityList(Integer type) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityInfo::getTenantId, tenantId)
                .eq(ActivityInfo::getStatus, 1)
                .le(ActivityInfo::getStartTime, now)
                .ge(ActivityInfo::getEndTime, now);
        if (type != null) {
            wrapper.eq(ActivityInfo::getType, type);
        }
        wrapper.orderByDesc(ActivityInfo::getCreateTime);
        List<ActivityInfo> activities = activityInfoMapper.selectList(wrapper);

        return BeanConverter.convertList(activities, ActivityInfoVO.class);
    }

    @Override
    public ActivityInfoVO getActivityDetail(Long activityId) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "活动不存在");
        }
        return BeanConverter.convert(activity, ActivityInfoVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createActivityOrder(ActivityOrderCreateReq req) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        ActivityInfo activity = activityInfoMapper.selectById(req.getActivityId());
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "活动不存在");
        }
        if (activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动未开始或已结束");
        }
        if (req.getNum() > activity.getLimitNum()) {
            throw new BusinessException("超过限购数量");
        }

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, req.getActivityId());
        Long stock = stringRedisTemplate.opsForValue().decrement(stockKey, req.getNum());
        if (stock == null || stock < 0) {
            stringRedisTemplate.opsForValue().increment(stockKey, req.getNum());
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        String lockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, req.getActivityId());
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                stringRedisTemplate.opsForValue().increment(stockKey, req.getNum());
                throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
            }

            BigDecimal totalAmount = activity.getPrice().multiply(BigDecimal.valueOf(req.getNum()));
            String orderNo = OrderNoUtil.generateOrderNo();

            OrderInfo order = new OrderInfo();
            order.setTenantId(tenantId);
            order.setOrderNo(orderNo);
            order.setMemberId(memberId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            order.setPayType(PayTypeEnum.WECHAT.getCode());
            order.setPayStatus(PayStatusEnum.UNPAID.getCode());
            orderInfoMapper.insert(order);

            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(order.getId());
            orderItem.setGoodsId(activity.getGoodsId());
            orderItem.setGoodsName(activity.getTitle());
            orderItem.setNum(req.getNum());
            orderItem.setPrice(activity.getPrice());
            orderItemMapper.insert(orderItem);

            ActivityOrder activityOrder = new ActivityOrder();
            activityOrder.setTenantId(tenantId);
            activityOrder.setActivityId(req.getActivityId());
            activityOrder.setOrderId(order.getId());
            activityOrder.setMemberId(memberId);
            activityOrder.setStatus(ActivityOrderStatusEnum.UNPAID.getCode());
            activityOrderMapper.insert(activityOrder);

            Map<String, String> wechatPayParams = wechatPayService.createMiniProgramPayOrder(order);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("payAmount", totalAmount);
            result.put("wechatPayParams", wechatPayParams);

            log.info("活动订单创建成功，订单号：{}，活动ID：{}", orderNo, req.getActivityId());
            return result;

        } catch (InterruptedException e) {
            stringRedisTemplate.opsForValue().increment(stockKey, req.getNum());
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public UserAccountBalanceVO getAccountBalance() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        LambdaQueryWrapper<MemberAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAccount::getTenantId, tenantId)
                .eq(MemberAccount::getMemberId, memberId);
        MemberAccount account = memberAccountMapper.selectOne(wrapper);

        if (account == null) {
            return UserAccountBalanceVO.builder()
                    .balance(java.math.BigDecimal.ZERO)
                    .totalRecharge(java.math.BigDecimal.ZERO)
                    .build();
        }

        return UserAccountBalanceVO.builder()
                .balance(account.getBalance())
                .totalRecharge(account.getTotalRecharge())
                .build();
    }

    @Override
    public List<UserMemberCardVO> getMemberCardList() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getTenantId, tenantId)
                .eq(MemberCard::getMemberId, memberId)
                .orderByDesc(MemberCard::getCreateTime);
        List<MemberCard> cards = memberCardMapper.selectList(wrapper);

        return cards.stream().map(card -> {
            CardRule rule = cardRuleMapper.selectById(card.getCardRuleId());
            Integer status;
            if (card.getRemainTimes() <= 0) {
                status = 0;
            } else if (now.isAfter(card.getExpireTime())) {
                status = 2;
            } else {
                status = 1;
            }
            return UserMemberCardVO.builder()
                    .id(card.getId())
                    .cardRuleId(card.getCardRuleId())
                    .cardName(rule != null ? rule.getName() : "次卡")
                    .remainTimes(card.getRemainTimes())
                    .expireTime(card.getExpireTime())
                    .status(status)
                    .build();
        }).toList();
    }

    @Override
    public List<OrderInfoVO> getOrderList() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getTenantId, tenantId)
                .eq(OrderInfo::getMemberId, memberId)
                .orderByDesc(OrderInfo::getCreateTime);
        List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);

        return BeanConverter.convertList(orders, OrderInfoVO.class);
    }

    @Override
    public List<PetInfoVO> getPetList() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        LambdaQueryWrapper<PetInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetInfo::getTenantId, tenantId)
                .eq(PetInfo::getMemberId, memberId)
                .orderByDesc(PetInfo::getCreateTime);
        List<PetInfo> pets = petInfoMapper.selectList(wrapper);

        return BeanConverter.convertList(pets, PetInfoVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyCodeVO generateVerifyCode(GenerateVerifyCodeReq req) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long memberId = (Long) StpUtil.getSession().get("member_id");

        MemberCard memberCard = memberCardMapper.selectById(req.getMemberCardId());
        if (memberCard == null || !memberCard.getTenantId().equals(tenantId) || !memberCard.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_CARD_NOT_FOUND);
        }
        if (memberCard.getRemainTimes() <= 0) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }
        if (LocalDateTime.now().isAfter(memberCard.getExpireTime())) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }

        LambdaQueryWrapper<VerifyCodeRecord> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(VerifyCodeRecord::getTenantId, tenantId)
                .eq(VerifyCodeRecord::getMemberCardId, req.getMemberCardId())
                .eq(VerifyCodeRecord::getStatus, VerifyCodeStatusEnum.UNUSED.getCode());
        List<VerifyCodeRecord> oldRecords = verifyCodeRecordMapper.selectList(oldWrapper);
        for (VerifyCodeRecord old : oldRecords) {
            old.setStatus(VerifyCodeStatusEnum.INVALID.getCode());
            verifyCodeRecordMapper.updateById(old);
        }

        String code = VerifyCodeUtil.generateCode();
        LocalDateTime expireTime = VerifyCodeUtil.calculateExpireTime();

        VerifyCodeRecord record = new VerifyCodeRecord();
        record.setTenantId(tenantId);
        record.setVerifyCode(code);
        record.setMemberCardId(req.getMemberCardId());
        record.setMemberId(memberId);
        record.setExpireTime(expireTime);
        record.setStatus(VerifyCodeStatusEnum.UNUSED.getCode());
        verifyCodeRecordMapper.insert(record);

        CardRule rule = cardRuleMapper.selectById(memberCard.getCardRuleId());

        return VerifyCodeVO.builder()
                .verifyCode(code)
                .expireTime(expireTime)
                .cardName(rule != null ? rule.getName() : "次卡")
                .remainTimes(memberCard.getRemainTimes())
                .build();
    }

    @Override
    public AiChatRespVO aiChat(AiChatReq req) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String limitKey = String.format(RedisKeyConstants.AI_LIMIT_KEY, tenantId, today);

        String countStr = stringRedisTemplate.opsForValue().get(limitKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        if (count >= 50) {
            throw new BusinessException(ErrorCode.AI_LIMIT_EXCEEDED);
        }

        stringRedisTemplate.opsForValue().increment(limitKey);

        return AiChatRespVO.builder()
                .answer("AI客服功能开发中，敬请期待...")
                .build();
    }

    @Override
    public List<AiChatHistoryVO> getAiChatHistory() {
        return List.of();
    }
}

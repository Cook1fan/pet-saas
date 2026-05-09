package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.controller.pc.ActivityController;
import com.pet.saas.dto.query.ActivityQuery;
import com.pet.saas.entity.ActivityInfo;
import com.pet.saas.entity.ActivityOrder;
import com.pet.saas.mapper.ActivityInfoMapper;
import com.pet.saas.mapper.ActivityOrderMapper;
import com.pet.saas.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityService {

    private final ActivityInfoMapper activityInfoMapper;
    private final ActivityOrderMapper activityOrderMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityInfo createGroupActivity(ActivityController.CreateGroupActivityReq req, Long tenantId) {
        ActivityInfo activity = new ActivityInfo();
        activity.setTenantId(tenantId);
        activity.setType(1);
        activity.setTitle(req.getTitle());
        activity.setGoodsId(req.getGoodsId());
        activity.setSkuId(req.getSkuId());
        activity.setPrice(req.getPrice());
        activity.setOriginPrice(req.getOriginPrice());
        activity.setGroupCount(req.getGroupCount());
        activity.setGroupValidHours(req.getGroupValidHours() != null ? req.getGroupValidHours() : 24);
        activity.setStock(req.getStock());
        activity.setLimitNum(req.getLimitNum());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setStatus(0);
        activityInfoMapper.insert(activity);

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
        redisTemplate.opsForValue().set(stockKey, req.getStock());

        return activityInfoMapper.selectById(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityInfo createSeckillActivity(ActivityController.CreateSeckillActivityReq req, Long tenantId) {
        ActivityInfo activity = new ActivityInfo();
        activity.setTenantId(tenantId);
        activity.setType(2);
        activity.setTitle(req.getTitle());
        activity.setGoodsId(req.getGoodsId());
        activity.setPrice(req.getPrice());
        activity.setOriginPrice(req.getOriginPrice());
        activity.setStock(req.getStock());
        activity.setLimitNum(req.getLimitNum());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setStatus(0);
        activityInfoMapper.insert(activity);

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
        redisTemplate.opsForValue().set(stockKey, req.getStock());

        return activityInfoMapper.selectById(activity.getId());
    }

    @Override
    public Page<ActivityInfo> listActivities(ActivityQuery query, Long tenantId) {
        LambdaQueryWrapper<ActivityInfo> wrapper = new LambdaQueryWrapper<>();
        if (query.getType() != null) {
            wrapper.eq(ActivityInfo::getType, query.getType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ActivityInfo::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ActivityInfo::getCreateTime);
        return activityInfoMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public ActivityInfo getActivity(Long activityId) {
        return activityInfoMapper.selectById(activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityOrder createActivityOrder(Long tenantId, Long activityId, Long memberId, Long orderId) {
        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activityId);
        Long stock = redisTemplate.opsForValue().decrement(stockKey);

        if (stock == null || stock < 0) {
            redisTemplate.opsForValue().increment(stockKey);
            throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
        }

        String lockKey = String.format(RedisKeyConstants.ORDER_USER_LOCK_KEY, memberId, activityId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                redisTemplate.opsForValue().increment(stockKey);
                throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
            }

            ActivityOrder activityOrder = new ActivityOrder();
            activityOrder.setTenantId(tenantId);
            activityOrder.setActivityId(activityId);
            activityOrder.setOrderId(orderId);
            activityOrder.setMemberId(memberId);
            activityOrder.setStatus(0);
            activityOrderMapper.insert(activityOrder);

            return activityOrderMapper.selectById(activityOrder.getId());
        } catch (InterruptedException e) {
            redisTemplate.opsForValue().increment(stockKey);
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityInfo updateGroupActivity(Long activityId, ActivityController.CreateGroupActivityReq req, Long tenantId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activity.setTitle(req.getTitle());
        activity.setGoodsId(req.getGoodsId());
        activity.setSkuId(req.getSkuId());
        activity.setPrice(req.getPrice());
        activity.setOriginPrice(req.getOriginPrice());
        activity.setGroupCount(req.getGroupCount());
        activity.setGroupValidHours(req.getGroupValidHours() != null ? req.getGroupValidHours() : 24);
        activity.setStock(req.getStock());
        activity.setLimitNum(req.getLimitNum());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activityInfoMapper.updateById(activity);

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
        redisTemplate.opsForValue().set(stockKey, req.getStock());

        return activityInfoMapper.selectById(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityInfo updateSeckillActivity(Long activityId, ActivityController.CreateSeckillActivityReq req, Long tenantId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activity.setTitle(req.getTitle());
        activity.setGoodsId(req.getGoodsId());
        activity.setPrice(req.getPrice());
        activity.setOriginPrice(req.getOriginPrice());
        activity.setStock(req.getStock());
        activity.setLimitNum(req.getLimitNum());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activityInfoMapper.updateById(activity);

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activity.getId());
        redisTemplate.opsForValue().set(stockKey, req.getStock());

        return activityInfoMapper.selectById(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long activityId, Long tenantId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activityInfoMapper.deleteById(activityId);

        String stockKey = String.format(RedisKeyConstants.ACTIVITY_STOCK_KEY, activityId);
        redisTemplate.delete(stockKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus(Long activityId, Integer status, Long tenantId) {
        ActivityInfo activity = activityInfoMapper.selectById(activityId);
        if (activity == null || !activity.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND);
        }
        activity.setStatus(status);
        activityInfoMapper.updateById(activity);
    }

    @Override
    public Map<String, Object> getActivityData(Long activityId) {
        long orderCount = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>().eq(ActivityOrder::getActivityId, activityId));
        long paidCount = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .in(ActivityOrder::getStatus, 1, 2));
        long successCount = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getStatus, 2));
        long failCount = activityOrderMapper.selectCount(
                new LambdaQueryWrapper<ActivityOrder>()
                        .eq(ActivityOrder::getActivityId, activityId)
                        .eq(ActivityOrder::getStatus, 3));

        Map<String, Object> data = new HashMap<>();
        data.put("joinCount", orderCount);
        data.put("orderCount", orderCount);
        data.put("paidCount", paidCount);
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        return data;
    }
}

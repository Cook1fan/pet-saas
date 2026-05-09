package com.pet.saas.job;

import com.pet.saas.common.util.TenantContext;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultHandler;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 订单超时取消定时任务
 */
@Slf4j
@Component
public class OrderTimeoutCancelJob {

    private final OrderService orderService;
    private final RedissonClient redissonClient;
    private final OrderTimeoutCancelJob self;

    public OrderTimeoutCancelJob(
            OrderService orderService,
            RedissonClient redissonClient,
            @Lazy OrderTimeoutCancelJob self) {
        this.orderService = orderService;
        this.redissonClient = redissonClient;
        this.self = self;
    }

    /**
     * 每分钟执行一次，取消超时未支付订单
     * 使用 Redisson 分布式锁保证多实例环境下只有一个实例执行
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cancelTimeoutOrders() {
        String lockKey = "job:order:timeout:cancel";
        RLock lock = redissonClient.getLock(lockKey);

        // 尝试获取锁，等待时间3秒，锁持有时间5分钟
        try {
            if (!lock.tryLock(3, 300, TimeUnit.SECONDS)) {
                log.info("其他实例正在执行订单超时取消任务，跳过本次执行");
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断", e);
            return;
        }

        try {
            log.info("开始执行订单超时取消任务");
            self.doCancelTimeoutOrders();
            log.info("订单超时取消任务执行完成");
        } catch (Exception e) {
            log.error("订单超时取消任务执行异常", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 流式查询处理超时订单
     * 使用 @Transactional 保证流式查询在事务内执行
     */
    @Transactional(readOnly = true)
    protected void doCancelTimeoutOrders() {
        int[] processedCount = {0};

        // 开启忽略多租户插件（整个定时任务流程都忽略）
        TenantContext.ignore();
        try {
            // 使用 ResultHandler 流式处理，避免一次性加载大量数据到内存
            orderService.selectTimeoutUnpaidOrders(context -> {
                OrderInfo order = context.getResultObject();

                try {
                    // 取消订单（使用普通业务方法，忽略多租户插件已开启）
                    orderService.cancelOrder(order.getId(), "订单超时自动取消", true);
                    processedCount[0]++;
                } catch (Exception e) {
                    log.error("取消订单失败，orderId={}", order.getId(), e);
                }
            });
        } finally {
            // 恢复多租户插件
            TenantContext.restore();
        }

        log.info("本次订单超时取消任务处理数量：{}", processedCount[0]);
    }
}

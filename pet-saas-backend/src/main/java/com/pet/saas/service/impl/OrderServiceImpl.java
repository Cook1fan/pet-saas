package com.pet.saas.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.enums.*;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.dto.query.OrderQuery;
import com.pet.saas.dto.req.PcOrderCreateReq;
import com.pet.saas.dto.req.PcOrderItemRequest;
import com.pet.saas.dto.req.PcOrderRefundReq;
import com.pet.saas.dto.req.StockInReq;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.MemberAccountMapper;
import com.pet.saas.mapper.MemberCardMapper;
import com.pet.saas.mapper.OrderInfoMapper;
import com.pet.saas.mapper.OrderItemMapper;
import com.pet.saas.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ResultHandler;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final GoodsSkuService goodsSkuService;
    private final GoodsService goodsService;
    private final MemberAccountMapper memberAccountMapper;
    private final MemberCardMapper memberCardMapper;
    private final FlowRecordService flowRecordService;
    private final OrderOperationLogService orderOperationLogService;
    private final StockService stockService;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrder(PcOrderCreateReq req, Long tenantId) {
        // 获取操作人信息
        Long operatorId = null;
        String operatorName = null;
        if (StpUtil.isLogin()) {
            operatorId = StpUtil.getLoginIdAsLong();
            // TODO: 如果需要 operatorName，需要从用户表查询
        }

        // 分布式锁防止重复下单
        String lockKey = String.format("order:create:lock:%d:%s", tenantId,
                req.getMemberId() != null ? req.getMemberId() : "guest");
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "您有一笔订单正在处理中，请稍候");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        try {
            return this.doCreateOrder(req, tenantId, operatorId, operatorName);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 内部创建订单方法
     */
    private OrderInfo doCreateOrder(PcOrderCreateReq req, Long tenantId, Long operatorId, String operatorName) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. 校验商品并缓存SKU信息（只查询一次）
        List<Long> skuIds = req.getItems().stream()
                .map(PcOrderItemRequest::getSkuId)
                .collect(Collectors.toList());
        List<GoodsSku> skuList = goodsSkuService.listBySkuIds(skuIds);
        Map<Long, GoodsSku> skuMap = skuList.stream()
                .collect(Collectors.toMap(GoodsSku::getId, s -> s));

        // 2. 校验商品并获取goodsId列表
        List<Long> goodsIds = new ArrayList<>();
        for (PcOrderItemRequest item : req.getItems()) {
            GoodsSku sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "商品SKU不存在");
            }
            // 校验库存（用已查询的sku，不重复查库）
            if (!Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
                int stock = sku.getStock() != null ? sku.getStock() : 0;
                int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;
                if (stock - reservedStock < item.getNum()) {
                    throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT);
                }
            }
            totalAmount = totalAmount.add(sku.getPrice().multiply(BigDecimal.valueOf(item.getNum())));
            goodsIds.add(sku.getGoodsId());
        }

        // 3. 查询商品信息（获取商品名）
        List<Goods> goodsList = goodsService.listByGoodsIds(goodsIds);
        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        // 4. 支付方式前置校验
        if (PayTypeEnum.RECHARGE.getCode() == req.getPayType()) {
            if (req.getMemberId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "储值支付需选择会员");
            }
            MemberAccount account = memberAccountMapper.selectOne(
                    new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, req.getMemberId()));
            if (account == null || account.getBalance().compareTo(totalAmount) < 0) {
                throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
            }
        } else if (PayTypeEnum.CARD.getCode() == req.getPayType()) {
            if (req.getCardId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "次卡支付需选择次卡");
            }
            MemberCard card = memberCardMapper.selectById(req.getCardId());
            if (card == null || card.getRemainTimes() <= 0 || card.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
            }
        }

        // 5. 生成订单号
        String orderNo = OrderNoUtil.generateOrderNo();

        // 6. 判断订单初始状态和库存处理方式
        int initialOrderStatus;
        boolean isImmediatePay = PayTypeEnum.CASH.getCode() == req.getPayType()
                || PayTypeEnum.RECHARGE.getCode() == req.getPayType()
                || PayTypeEnum.CARD.getCode() == req.getPayType();

        if (isImmediatePay) {
            // 现金、储值、次卡：直接发货状态
            initialOrderStatus = OrderStatusEnum.SHIPPED.getCode();
        } else {
            // 微信支付：待支付状态
            initialOrderStatus = OrderStatusEnum.UNPAID.getCode();
        }

        // 7. 创建订单
        OrderInfo order = new OrderInfo();
        order.setTenantId(tenantId);
        order.setOrderNo(orderNo);
        order.setMemberId(req.getMemberId());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setPayType(req.getPayType());
        order.setPayStatus(isImmediatePay ? PayStatusEnum.PAID.getCode() : PayStatusEnum.UNPAID.getCode());
        order.setOrderSource(OrderSourceEnum.PC.getCode());
        order.setOrderStatus(initialOrderStatus);
        if (!isImmediatePay) {
            // 微信支付订单15分钟过期
            order.setExpireTime(LocalDateTime.now().plusMinutes(15));
        }
        orderInfoMapper.insert(order);

        // 8. 创建订单项 & 处理库存
        for (PcOrderItemRequest item : req.getItems()) {
            Long skuId = item.getSkuId();
            int num = item.getNum();

            GoodsSku sku = skuMap.get(skuId);
            Goods goods = goodsMap.get(sku.getGoodsId());

            OrderItem orderItem = new OrderItem();
            orderItem.setTenantId(tenantId);
            orderItem.setOrderId(order.getId());
            orderItem.setGoodsId(sku.getGoodsId());
            orderItem.setSkuId(skuId);
            orderItem.setBarcode(sku.getBarcode());
            // 商品名格式：商品名 + 规格名 + 规格值
            String goodsName = goods.getGoodsName();
            if (StrUtil.isNotBlank(sku.getSpecName())) {
                goodsName += " " + sku.getSpecName();
            }
            if (StrUtil.isNotBlank(sku.getSpecValue())) {
                goodsName += " " + sku.getSpecValue();
            }
            orderItem.setGoodsName(goodsName);
            orderItem.setNum(num);
            orderItem.setPrice(sku.getPrice());
            orderItem.setCostPrice(sku.getCostPrice());
            orderItemMapper.insert(orderItem);

            // 处理库存
            if (!sku.getIsUnlimitedStock().equals(1)) {
                if (isImmediatePay) {
                    // 即时支付：直接扣减可用库存，生成stock_record
                    stockService.deductStockDirect(skuId, num, tenantId, operatorId, orderNo);
                } else {
                    // 微信支付：预占库存，不生成stock_record
                    stockService.reserveStock(skuId, num, tenantId, operatorId, orderNo);
                }
            }
        }

        // 9. 根据支付方式处理
        if (PayTypeEnum.CASH.getCode() == req.getPayType()) {
            // 现金支付：直接完成订单
            this.completeOrderInternal(order, null, operatorId, operatorName);
        } else if (PayTypeEnum.RECHARGE.getCode() == req.getPayType()) {
            // 储值支付：扣余额 + 完成订单
            int affected = memberAccountMapper.deductBalance(req.getMemberId(), totalAmount);
            if (affected == 0) {
                throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
            }
            this.completeOrderInternal(order, null, operatorId, operatorName);
        } else if (PayTypeEnum.CARD.getCode() == req.getPayType()) {
            // 次卡支付：扣次数 + 完成订单
            int affected = memberCardMapper.deductTimes(req.getCardId());
            if (affected == 0) {
                throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
            }
            this.completeOrderInternal(order, null, operatorId, operatorName);
        }

        // 10. 记录创建订单操作日志
        orderOperationLogService.saveLog(
                order,
                null,
                initialOrderStatus,
                OrderOperationTypeEnum.CREATE.getCode(),
                "创建订单",
                operatorId,
                operatorName
        );

        log.info("开单成功，订单号：{}，支付方式：{}，订单状态：{}", orderNo, req.getPayType(), initialOrderStatus);
        return orderInfoMapper.selectById(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(OrderInfo order, String transactionId) {
        Long operatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        this.completeOrderInternal(order, transactionId, operatorId, null);
    }

    /**
     * 内部完成订单方法
     */
    private void completeOrderInternal(OrderInfo order, String transactionId, Long operatorId, String operatorName) {
        Integer beforeStatus = order.getOrderStatus();
        int targetStatus;

        // 判断目标状态：PC端直接到已发货，小程序端到已支付
        if (Objects.equals(OrderSourceEnum.PC.getCode(), order.getOrderSource())) {
            targetStatus = OrderStatusEnum.SHIPPED.getCode();
        } else {
            targetStatus = OrderStatusEnum.PAID.getCode();
        }

        // 状态机校验
        OrderStatusEnum currentStatusEnum = OrderStatusEnum.of(beforeStatus);
        OrderStatusEnum targetStatusEnum = OrderStatusEnum.of(targetStatus);
        if (currentStatusEnum == null || !currentStatusEnum.canTransitionTo(targetStatusEnum)) {
            log.warn("订单状态不允许变更，orderId={}, currentStatus={}, targetStatus={}", order.getId(), beforeStatus, targetStatus);
            return;
        }

        // 更新订单
        order.setOrderStatus(targetStatus);
        order.setPayStatus(PayStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        order.setTransactionId(transactionId);
        orderInfoMapper.updateById(order);

        // PC端微信支付：确认扣减预留库存（从reservedStock移到真正扣减）
        if (Objects.equals(OrderSourceEnum.PC.getCode(), order.getOrderSource())
                && Objects.equals(OrderStatusEnum.UNPAID.getCode(), beforeStatus)) {
            this.confirmDeductStockForOrder(order, operatorId, operatorName);
        }

        // 生成流水
        flowRecordService.createFlowRecord(order, FlowTypeEnum.CONSUME.getCode(), transactionId);

        // 记录操作日志
        orderOperationLogService.saveLog(
                order,
                beforeStatus,
                targetStatus,
                OrderOperationTypeEnum.PAY.getCode(),
                "支付成功",
                operatorId,
                operatorName
        );

        log.info("订单完成，订单号：{}，交易单号：{}", order.getOrderNo(), transactionId);
    }

    /**
     * 确认扣减订单的预留库存
     */
    private void confirmDeductStockForOrder(OrderInfo order, Long operatorId, String operatorName) {
        List<OrderItem> items = this.getOrderItems(order.getId());

        for (OrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            // 确认扣减：扣减stock，减少reservedStock，生成stock_record（StockService内部会处理SKU查询和无限库存判断）
            stockService.confirmDeductStock(item.getSkuId(), item.getNum(),
                    order.getTenantId(), operatorId, order.getOrderNo());
        }
    }

    @Override
    public Page<OrderInfo> listOrders(OrderQuery query, Long tenantId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        if (query.getMemberId() != null) {
            wrapper.eq(OrderInfo::getMemberId, query.getMemberId());
        }
        if (query.getPayType() != null) {
            wrapper.eq(OrderInfo::getPayType, query.getPayType());
        }
        if (query.getPayStatus() != null) {
            wrapper.eq(OrderInfo::getPayStatus, query.getPayStatus());
        }
        if (query.getOrderStatus() != null) {
            wrapper.eq(OrderInfo::getOrderStatus, query.getOrderStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(OrderInfo::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(OrderInfo::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public OrderInfo getOrderDetail(Long orderId) {
        return orderInfoMapper.selectById(orderId);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(PcOrderRefundReq req) {
        OrderInfo order = orderInfoMapper.selectById(req.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getPayStatus() != PayStatusEnum.PAID.getCode()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "订单不支持退款");
        }

        Long operatorId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        Integer beforeStatus = order.getOrderStatus();

        // 更新订单状态
        order.setPayStatus(PayStatusEnum.REFUNDED.getCode());
        order.setOrderStatus(OrderStatusEnum.REFUNDED.getCode());
        orderInfoMapper.updateById(order);

        // 退回库存（通过StockService处理）
        List<OrderItem> items = this.getOrderItems(req.getOrderId());
        for (OrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            GoodsSku sku = goodsSkuService.getSku(item.getSkuId());
            if (sku != null && !sku.getIsUnlimitedStock().equals(1)) {
                // 使用StockInReq退回库存
                StockInReq stockInReq = new StockInReq();
                stockInReq.setSkuId(item.getSkuId());
                stockInReq.setNum(item.getNum());
                stockInReq.setType(StockChangeTypeEnum.RETURN_IN.getCode());
                stockInReq.setRelatedType("order");
                stockInReq.setRelatedId(order.getId());
                stockInReq.setRelatedNo(order.getOrderNo());
                stockInReq.setRemark("订单退款：" + req.getReason());
                stockService.inStock(stockInReq, order.getTenantId(), operatorId);
            }
        }

        // 退回余额（储值支付）
        if (order.getPayType() == PayTypeEnum.RECHARGE.getCode() && order.getMemberId() != null) {
            memberAccountMapper.addBalance(order.getMemberId(), req.getRefundAmount());
        }

        // 生成退款流水
        flowRecordService.createFlowRecord(order, FlowTypeEnum.REFUND.getCode(), null);

        // 记录操作日志
        orderOperationLogService.saveLog(
                order,
                beforeStatus,
                OrderStatusEnum.REFUNDED.getCode(),
                OrderOperationTypeEnum.REFUND.getCode(),
                "订单退款：" + req.getReason(),
                operatorId,
                null
        );

        log.info("订单退款成功，订单号：{}，退款金额：{}", order.getOrderNo(), req.getRefundAmount());
    }

    @Override
    @Transactional(
            rollbackFor = Exception.class,
            propagation = Propagation.REQUIRES_NEW  // 关键！新建独立可写事务
    )
    public void cancelOrder(Long orderId, String reason, boolean isSystem) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "订单不存在");
        }

        Integer beforeStatus = order.getOrderStatus();
        OrderStatusEnum currentStatus = OrderStatusEnum.of(beforeStatus);
        if (currentStatus == null || !currentStatus.canTransitionTo(OrderStatusEnum.CANCELLED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "当前订单状态不允许取消");
        }

        Long operatorId = isSystem ? null : (StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        String operatorName = isSystem ? "系统" : null;

        // 释放预占库存（仅待支付状态且是微信支付）
        if (Objects.equals(OrderStatusEnum.UNPAID.getCode(), beforeStatus)) {
            this.releaseReservedStockForOrder(order, operatorId, operatorName);
        }

        // 更新订单状态
        order.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        orderInfoMapper.updateById(order);

        // 记录操作日志
        orderOperationLogService.saveLog(
                order,
                beforeStatus,
                OrderStatusEnum.CANCELLED.getCode(),
                OrderOperationTypeEnum.CANCEL.getCode(),
                reason,
                operatorId,
                operatorName
        );

        log.info("订单取消成功，订单号：{}，原因：{}", order.getOrderNo(), reason);
    }

    /**
     * 释放订单的预占库存
     */
    private void releaseReservedStockForOrder(OrderInfo order, Long operatorId, String operatorName) {
        List<OrderItem> items = this.getOrderItems(order.getId());

        for (OrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            // 释放预占库存：不生成stock_record（StockService内部会处理SKU查询和无限库存判断）
            stockService.releaseReservedStock(item.getSkuId(), item.getNum(),
                    order.getTenantId(), operatorId, order.getOrderNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, Long operatorId, String operatorName) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "订单不存在");
        }

        Integer beforeStatus = order.getOrderStatus();
        OrderStatusEnum currentStatus = OrderStatusEnum.of(beforeStatus);
        if (currentStatus == null || !currentStatus.canTransitionTo(OrderStatusEnum.SHIPPED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "当前订单状态不允许发货");
        }

        // 确认扣减预留库存（小程序订单发货时扣减）
        if (Objects.equals(OrderStatusEnum.PAID.getCode(), beforeStatus)) {
            this.confirmDeductStockForOrder(order, operatorId, operatorName);
        }

        // 更新订单状态
        order.setOrderStatus(OrderStatusEnum.SHIPPED.getCode());
        orderInfoMapper.updateById(order);

        // 记录操作日志
        orderOperationLogService.saveLog(
                order,
                beforeStatus,
                OrderStatusEnum.SHIPPED.getCode(),
                OrderOperationTypeEnum.SHIP.getCode(),
                "订单发货",
                operatorId,
                operatorName
        );

        log.info("订单发货成功，订单号：{}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId, Long operatorId, String operatorName) {
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "订单不存在");
        }

        Integer beforeStatus = order.getOrderStatus();
        OrderStatusEnum currentStatus = OrderStatusEnum.of(beforeStatus);
        if (currentStatus == null || !currentStatus.canTransitionTo(OrderStatusEnum.COMPLETED)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "当前订单状态不允许完成");
        }

        // 更新订单状态
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        orderInfoMapper.updateById(order);

        // 记录操作日志
        orderOperationLogService.saveLog(
                order,
                beforeStatus,
                OrderStatusEnum.COMPLETED.getCode(),
                OrderOperationTypeEnum.COMPLETE.getCode(),
                "订单完成",
                operatorId,
                operatorName
        );

        log.info("订单完成，订单号：{}", order.getOrderNo());
    }

    @Override
    public void selectTimeoutUnpaidOrders(ResultHandler<OrderInfo> handler) {
        orderInfoMapper.selectCursor(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderStatus, OrderStatusEnum.UNPAID.getCode())
                        .le(OrderInfo::getExpireTime, LocalDateTime.now()),
                handler
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String orderNo, String transactionId) {
        OrderInfo order = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo)
        );

        if (order == null) {
            log.warn("支付回调订单不存在：{}", orderNo);
            return;
        }

        // 幂等性检查
        if (!Objects.equals(OrderStatusEnum.UNPAID.getCode(), order.getOrderStatus())) {
            log.info("订单已处理，直接返回，订单号：{}", orderNo);
            return;
        }

        this.completeOrderInternal(order, transactionId, null, null);
    }
}

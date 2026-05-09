package com.pet.saas.unit.service;

import com.pet.saas.base.BaseUnitTest;
import com.pet.saas.common.enums.PayStatusEnum;
import com.pet.saas.common.enums.PayTypeEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.dto.req.PcOrderCreateReq;
import com.pet.saas.dto.req.PcOrderItemRequest;
import com.pet.saas.dto.req.PcOrderRefundReq;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.FlowRecordService;
import com.pet.saas.service.GoodsSkuService;
import com.pet.saas.service.StockRecordService;
import com.pet.saas.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("订单服务测试")
class OrderServiceTest extends BaseUnitTest {

    @Mock
    private OrderInfoMapper orderInfoMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private GoodsSkuService goodsSkuService;

    @Mock
    private MemberAccountMapper memberAccountMapper;

    @Mock
    private MemberCardMapper memberCardMapper;

    @Mock
    private FlowRecordMapper flowRecordMapper;

    @Mock
    private FlowRecordService flowRecordService;

    @Mock
    private StockRecordService stockRecordService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("应该成功创建现金支付订单")
    void shouldCreateOrderSuccessfullyWithCashPay() {
        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 100);
        when(goodsSkuService.getSku(1L)).thenReturn(sku);
        when(goodsSkuService.checkStockAvailable(1L, 1)).thenReturn(true);
        when(goodsSkuService.calculateAvailableStock(sku)).thenReturn(100);
        when(goodsSkuService.deductStock(1L, 1)).thenReturn(true);

        when(orderInfoMapper.insert(any(OrderInfo.class))).thenAnswer(inv -> {
            OrderInfo order = inv.getArgument(0);
            order.setId(1L);
            return 1;
        });
        when(orderInfoMapper.selectById(1L)).thenAnswer(inv -> {
            OrderInfo order = new OrderInfo();
            order.setId(1L);
            order.setPayStatus(PayStatusEnum.PAID.getCode());
            return order;
        });

        PcOrderCreateReq req = new PcOrderCreateReq();
        req.setMemberId(1L);
        req.setPayType(PayTypeEnum.CASH.getCode());
        req.setItems(Arrays.asList(createOrderItemRequest(1L, 1)));

        OrderInfo result = orderService.createOrder(req, 1L);

        assertNotNull(result);
        verify(goodsSkuService, times(2)).getSku(1L);
        verify(orderInfoMapper, times(1)).insert(any(OrderInfo.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
    }

    @Test
    @DisplayName("当库存不足时应该拒绝创建订单")
    void shouldRejectOrderWhenStockInsufficient() {
        GoodsSku sku = createTestSku(1L, new BigDecimal("99.00"), 0);
        when(goodsSkuService.getSku(1L)).thenReturn(sku);
        when(goodsSkuService.checkStockAvailable(1L, 1)).thenReturn(false);

        PcOrderCreateReq req = new PcOrderCreateReq();
        req.setMemberId(1L);
        req.setPayType(PayTypeEnum.CASH.getCode());
        req.setItems(Arrays.asList(createOrderItemRequest(1L, 1)));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.createOrder(req, 1L));

        assertEquals(ErrorCode.STOCK_INSUFFICIENT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("当余额不足时应该拒绝储值支付订单")
    void shouldRejectOrderWhenBalanceInsufficient() {
        GoodsSku sku = createTestSku(1L, new BigDecimal("200.00"), 100);
        when(goodsSkuService.getSku(1L)).thenReturn(sku);
        when(goodsSkuService.checkStockAvailable(1L, 1)).thenReturn(true);

        MemberAccount account = createTestAccount(new BigDecimal("100.00"));
        when(memberAccountMapper.selectOne(any())).thenReturn(account);

        PcOrderCreateReq req = new PcOrderCreateReq();
        req.setMemberId(1L);
        req.setPayType(PayTypeEnum.RECHARGE.getCode());
        req.setItems(Arrays.asList(createOrderItemRequest(1L, 1)));

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.createOrder(req, 1L));

        assertEquals(ErrorCode.BALANCE_INSUFFICIENT.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("应该成功退款订单")
    void shouldRefundOrderSuccessfully() {
        OrderInfo order = createTestOrder(1L, PayStatusEnum.PAID.getCode());
        when(orderInfoMapper.selectById(1L)).thenReturn(order);
        when(orderItemMapper.selectList(any())).thenReturn(List.of());

        PcOrderRefundReq req = new PcOrderRefundReq();
        req.setOrderId(1L);
        req.setRefundAmount(new BigDecimal("99.00"));
        req.setReason("测试退款");

        assertDoesNotThrow(() -> orderService.refundOrder(req));

        verify(orderInfoMapper, times(1)).updateById(any(OrderInfo.class));
        verify(flowRecordService, times(1)).createFlowRecord(any(), eq(3), isNull());
    }

    @Test
    @DisplayName("当订单不是已支付状态时应该拒绝退款")
    void shouldRejectRefundWhenOrderNotPaid() {
        OrderInfo order = createTestOrder(1L, PayStatusEnum.UNPAID.getCode());
        when(orderInfoMapper.selectById(1L)).thenReturn(order);

        PcOrderRefundReq req = new PcOrderRefundReq();
        req.setOrderId(1L);
        req.setRefundAmount(new BigDecimal("99.00"));
        req.setReason("测试退款");

        BusinessException exception = assertThrows(BusinessException.class, () ->
                orderService.refundOrder(req));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), exception.getCode());
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

    private MemberAccount createTestAccount(BigDecimal balance) {
        MemberAccount account = new MemberAccount();
        account.setId(1L);
        account.setMemberId(1L);
        account.setBalance(balance);
        return account;
    }

    private OrderInfo createTestOrder(Long id, Integer payStatus) {
        OrderInfo order = new OrderInfo();
        order.setId(id);
        order.setTenantId(1L);
        order.setPayStatus(payStatus);
        order.setPayType(PayTypeEnum.CASH.getCode());
        order.setTotalAmount(new BigDecimal("99.00"));
        return order;
    }

    private PcOrderItemRequest createOrderItemRequest(Long skuId, int num) {
        PcOrderItemRequest item = new PcOrderItemRequest();
        item.setSkuId(skuId);
        item.setNum(num);
        return item;
    }
}

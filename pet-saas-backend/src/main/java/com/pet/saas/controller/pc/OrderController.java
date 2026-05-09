package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.OrderQuery;
import com.pet.saas.dto.req.PcOrderCreateReq;
import com.pet.saas.dto.req.PcOrderRefundReq;
import com.pet.saas.dto.resp.OrderInfoVO;
import com.pet.saas.dto.resp.OrderItemVO;
import com.pet.saas.dto.resp.PageResult;
import com.pet.saas.dto.resp.PcOrderDetailResp;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.entity.OrderItem;
import com.pet.saas.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "门店PC端-订单管理")
@RestController
@RequestMapping("/api/pc/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "开单收银")
    @PostMapping("/create")
    public R<OrderInfoVO> createOrder(@Valid @RequestBody PcOrderCreateReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        OrderInfo order = orderService.createOrder(req, tenantId);
        return R.ok(BeanConverter.convert(order, OrderInfoVO.class));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public R<PageResult<OrderInfoVO>> listOrders(@ParameterObject @Valid OrderQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<OrderInfo> page = orderService.listOrders(query, tenantId);
        return R.ok(BeanConverter.convertToPageResult(page, OrderInfoVO.class));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{orderId}")
    public R<PcOrderDetailResp> getOrderDetail(
            @Parameter(description = "订单ID", required = true) @PathVariable @NotNull Long orderId) {
        OrderInfo order = orderService.getOrderDetail(orderId);
        List<OrderItem> items = orderService.getOrderItems(orderId);
        PcOrderDetailResp resp = new PcOrderDetailResp();
        resp.setOrder(BeanConverter.convert(order, OrderInfoVO.class));
        resp.setItems(BeanConverter.convertList(items, OrderItemVO.class));
        return R.ok(resp);
    }

    @Operation(summary = "订单退款")
    @PostMapping("/refund")
    public R<Void> refundOrder(@Valid @RequestBody PcOrderRefundReq req) {
        orderService.refundOrder(req);
        return R.ok();
    }
}

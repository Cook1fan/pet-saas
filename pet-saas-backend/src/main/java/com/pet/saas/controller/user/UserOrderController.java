package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.OrderInfoVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-订单管理")
@RestController
@RequestMapping("/api/user/order")
@RequiredArgsConstructor
public class UserOrderController {

    private final UserService userService;

    @Operation(summary = "历史订单列表")
    @GetMapping("/list")
    public R<List<OrderInfoVO>> getOrderList() {
        List<OrderInfoVO> list = userService.getOrderList();
        return R.ok(list);
    }
}

package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.ShopSwitchReq;
import com.pet.saas.dto.resp.UserShopVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-门店管理")
@RestController
@RequestMapping("/api/user/shop")
@RequiredArgsConstructor
public class UserShopController {

    private final UserService userService;

    @Operation(summary = "已绑定门店列表")
    @GetMapping("/list")
    public R<List<UserShopVO>> getBoundShopList() {
        List<UserShopVO> list = userService.getBoundShopList();
        return R.ok(list);
    }

    @Operation(summary = "切换门店")
    @PostMapping("/switch")
    public R<Void> switchShop(@Valid @RequestBody ShopSwitchReq req) {
        userService.switchShop(req);
        return R.ok();
    }

    @Operation(summary = "可选门店列表（用于首次选择门店）")
    @GetMapping("/available")
    public R<List<UserShopVO>> getAvailableShopList() {
        List<UserShopVO> list = userService.getAvailableShopList();
        return R.ok(list);
    }

    @Operation(summary = "选择门店绑定（首次使用或切换）")
    @PostMapping("/bind")
    public R<Void> bindShop(@RequestBody ShopSwitchReq req) {
        userService.bindShop(req);
        return R.ok();
    }
}

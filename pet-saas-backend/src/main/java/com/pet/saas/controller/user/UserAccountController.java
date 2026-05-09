package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.UserAccountBalanceVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "C端小程序-账户管理")
@RestController
@RequestMapping("/api/user/account")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserService userService;

    @Operation(summary = "储值余额")
    @GetMapping("/balance")
    public R<UserAccountBalanceVO> getAccountBalance() {
        UserAccountBalanceVO vo = userService.getAccountBalance();
        return R.ok(vo);
    }
}

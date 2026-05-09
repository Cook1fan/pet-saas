package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.UserMemberCardVO;
import com.pet.saas.dto.resp.VerifyCodeVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-次卡管理")
@RestController
@RequestMapping("/api/user/card")
@RequiredArgsConstructor
public class UserCardController {

    private final UserService userService;

    @Operation(summary = "次卡列表")
    @GetMapping("/list")
    public R<List<UserMemberCardVO>> getMemberCardList() {
        List<UserMemberCardVO> list = userService.getMemberCardList();
        return R.ok(list);
    }

    @Operation(summary = "生成核销码")
    @GetMapping("/generateVerifyCode")
    public R<VerifyCodeVO> generateVerifyCode(
            @Parameter(description = "会员次卡ID", required = true)
            @RequestParam @NotNull Long memberCardId) {
        com.pet.saas.dto.req.GenerateVerifyCodeReq req = new com.pet.saas.dto.req.GenerateVerifyCodeReq();
        req.setMemberCardId(memberCardId);
        VerifyCodeVO vo = userService.generateVerifyCode(req);
        return R.ok(vo);
    }
}

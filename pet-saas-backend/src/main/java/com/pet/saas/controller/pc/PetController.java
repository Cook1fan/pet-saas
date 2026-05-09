package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.req.PetSaveReq;
import com.pet.saas.dto.resp.PetInfoVO;
import com.pet.saas.entity.PetInfo;
import com.pet.saas.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "门店PC端-宠物管理")
@RestController
@RequestMapping("/api/pc/pet")
@RequiredArgsConstructor
public class PetController {

    private final MemberService memberService;

    @Operation(summary = "宠物列表")
    @GetMapping("/list/{memberId}")
    public R<List<PetInfoVO>> listPets(
            @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        List<PetInfo> pets = memberService.listPets(tenantId, memberId);
        return R.ok(BeanConverter.convertList(pets, PetInfoVO.class));
    }

    @Operation(summary = "保存宠物档案")
    @PostMapping("/save")
    public R<Void> savePet(@Valid @RequestBody PetSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        PetInfo pet = BeanConverter.convert(req, PetInfo.class);
        memberService.savePet(tenantId, pet);
        return R.ok();
    }
}

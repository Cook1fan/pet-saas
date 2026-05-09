package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.PetInfoVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-宠物档案")
@RestController
@RequestMapping("/api/user/pet")
@RequiredArgsConstructor
public class UserPetController {

    private final UserService userService;

    @Operation(summary = "宠物档案列表")
    @GetMapping("/list")
    public R<List<PetInfoVO>> getPetList() {
        List<PetInfoVO> list = userService.getPetList();
        return R.ok(list);
    }
}

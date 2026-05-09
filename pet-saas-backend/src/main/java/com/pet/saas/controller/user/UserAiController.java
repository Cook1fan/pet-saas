package com.pet.saas.controller.user;

import com.pet.saas.common.R;
import com.pet.saas.dto.req.AiChatReq;
import com.pet.saas.dto.resp.AiChatHistoryVO;
import com.pet.saas.dto.resp.AiChatRespVO;
import com.pet.saas.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-AI客服")
@RestController
@RequestMapping("/api/user/ai")
@RequiredArgsConstructor
public class UserAiController {

    private final UserService userService;

    @Operation(summary = "AI客服对话")
    @PostMapping("/chat")
    public R<AiChatRespVO> aiChat(@Valid @RequestBody AiChatReq req) {
        AiChatRespVO vo = userService.aiChat(req);
        return R.ok(vo);
    }

    @Operation(summary = "历史对话记录")
    @GetMapping("/chatHistory")
    public R<List<AiChatHistoryVO>> getAiChatHistory() {
        List<AiChatHistoryVO> list = userService.getAiChatHistory();
        return R.ok(list);
    }
}

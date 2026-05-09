package com.pet.saas.controller.mp;

import cn.hutool.core.util.StrUtil;
import com.pet.saas.common.R;
import com.pet.saas.config.properties.WxMpProperties;
import com.pet.saas.dto.query.MpAuthorizeQuery;
import com.pet.saas.dto.query.MpCallbackQuery;
import com.pet.saas.dto.req.MpWxLoginReq;
import com.pet.saas.dto.resp.MpShopLoginRespVO;
import com.pet.saas.service.MpShopRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/api/mp")
@RequiredArgsConstructor
@Tag(name = "微信公众号认证接口", description = "微信公众号授权回调、登录等接口")
public class MpShopAuthController {

    private final MpShopRegisterService mpShopRegisterService;
    private final WxMpService wxMpService;
    private final WxMpProperties wxMpProperties;

    @Operation(summary = "微信公众号授权回调（重定向到H5）", description = "微信OAuth2授权回调，自动重定向到state指定的页面")
    @GetMapping("/wx/callback")
    public RedirectView wxCallbackRedirect(@ParameterObject @Valid MpCallbackQuery query) {
        log.info("收到微信公众号授权回调, code={}, state={}", query.getCode(), query.getState());
        String mpToken = mpShopRegisterService.handleWxCallback(query.getCode(), query.getState());

        // 重定向到 state 指定的页面，带上 mpToken
        RedirectView redirectView = new RedirectView();
        String redirectUrl = StrUtil.isNotBlank(query.getState()) ? query.getState() : "/shop-register-test.html";
        // 拼接 mpToken 参数
        if (StrUtil.contains(redirectUrl, "?")) {
            redirectUrl += "&mpToken=" + mpToken;
        } else {
            redirectUrl += "?mpToken=" + mpToken;
        }
        redirectView.setUrl(redirectUrl);
        log.info("重定向到: {}", redirectUrl);
        return redirectView;
    }

    @Operation(summary = "微信公众号授权回调（纯API）", description = "微信OAuth2授权回调接口，返回JSON格式的token")
    @GetMapping("/wx/callback/json")
    public R<String> wxCallbackJson(@ParameterObject @Valid MpCallbackQuery query) {
        log.info("收到微信公众号授权回调(JSON), code={}, state={}", query.getCode(), query.getState());
        String mpToken = mpShopRegisterService.handleWxCallback(query.getCode(), query.getState());
        return R.ok(mpToken);
    }

    @Operation(summary = "微信公众号商家登录", description = "已注册用户通过微信直接登录")
    @PostMapping("/shop/login")
    public R<MpShopLoginRespVO> login(@Valid @RequestBody MpWxLoginReq req) {
        log.info("微信公众号商家登录请求");
        MpShopLoginRespVO resp = mpShopRegisterService.login(req);
        return R.ok(resp);
    }

    @Operation(summary = "生成微信授权链接", description = "生成微信公众号OAuth2授权链接，用于前端跳转")
    @GetMapping("/authorize")
    public R<String> authorize(@ParameterObject @Valid MpAuthorizeQuery query) {
        log.info("生成微信授权链接, redirectUrl={}", query.getRedirectUrl());

        // 获取配置的回调地址，如果没配置则使用默认回调
        String callbackUrl = wxMpProperties.getCallbackUrl();
        if (StrUtil.isBlank(callbackUrl)) {
            callbackUrl = "https://your-domain.com/api/mp/wx/callback";
            log.warn("微信授权回调地址未配置(wx.mp.callback-url)，使用默认值: {}", callbackUrl);
        }

        // 构造授权链接：snsapi_userinfo 可获取用户详细信息
        // state 参数用于传递前端的跳转地址
        String authorizeUrl = wxMpService.getOAuth2Service().buildAuthorizationUrl(
                callbackUrl,
                WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                query.getRedirectUrl()
        );

        log.info("微信授权链接生成成功: {}", authorizeUrl);
        return R.ok(authorizeUrl);
    }
}

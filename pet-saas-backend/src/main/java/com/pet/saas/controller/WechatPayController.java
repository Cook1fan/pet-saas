package com.pet.saas.controller;

import com.pet.saas.service.WechatPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 微信支付回调控制器
 */
@Slf4j
@Tag(name = "微信支付回调")
@RestController
@RequestMapping("/api/wechat-pay")
@RequiredArgsConstructor
public class WechatPayController {

    private final WechatPayService wechatPayService;

    /**
     * 微信支付回调接口
     * <p>
     * 微信服务器会 POST 回调此接口，通知支付结果
     * </p>
     *
     * @param xmlData 微信回调 XML 数据
     * @return 响应给微信的 XML
     */
    @Operation(summary = "微信支付回调")
    @PostMapping(value = "/callback", produces = "application/xml")
    public String payCallback(@RequestBody String xmlData) {
        log.info("收到微信支付回调请求");
        try {
            return wechatPayService.handlePayCallback(xmlData);
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return """
                    <xml>
                      <return_code><![CDATA[FAIL]]></return_code>
                      <return_msg><![CDATA[处理失败]]></return_msg>
                    </xml>
                    """;
        }
    }
}

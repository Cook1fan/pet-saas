package com.pet.saas.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wx.mp")
public class WxMpProperties {

    /**
     * 微信公众号的appid (标准命名)
     */
    private String appId;

    /**
     * 微信公众号的appid (兼容旧配置名)
     */
    private String appid;

    /**
     * 微信公众号的Secret
     */
    private String secret;

    /**
     * 微信公众号消息服务器配置的token
     */
    private String token;

    /**
     * 微信公众号消息服务器配置的EncodingAESKey (标准命名)
     */
    private String aesKey;

    /**
     * 微信公众号消息服务器配置的EncodingAESKey (兼容旧配置名)
     */
    private String encodingAesKey;

    /**
     * 消息格式，XML或者JSON
     */
    private String msgDataFormat = "JSON";

    /**
     * 注册成功通知模板消息ID
     */
    private String registerSuccessTemplateId;

    /**
     * 微信授权回调地址（完整URL，如：https://your-domain.com/api/mp/wx/callback）
     */
    private String callbackUrl;

    /**
     * 获取实际使用的 appId，优先使用 appId，兼容 appid
     */
    public String getEffectiveAppId() {
        if (appId != null && !appId.isEmpty()) {
            return appId;
        }
        return appid;
    }

    /**
     * 获取实际使用的 aesKey，优先使用 aesKey，兼容 encodingAesKey
     */
    public String getEffectiveAesKey() {
        if (aesKey != null && !aesKey.isEmpty()) {
            return aesKey;
        }
        return encodingAesKey;
    }
}
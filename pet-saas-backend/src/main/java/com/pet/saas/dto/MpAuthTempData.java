package com.pet.saas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 微信公众号授权临时数据（存储在Redis中）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpAuthTempData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 微信unionid
     */
    private String unionid;

    /**
     * 微信昵称
     */
    private String nickname;

    /**
     * 微信头像
     */
    private String avatar;
}

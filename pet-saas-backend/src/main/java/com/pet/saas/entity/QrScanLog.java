package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 二维码扫码日志表
 */
@Data
@TableName("qr_scan_log")
public class QrScanLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 二维码 ID
     */
    private Long qrId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 扫码人微信 openid（如已关注公众号）
     */
    private String openid;

    /**
     * 微信 unionid
     */
    private String unionid;

    /**
     * 会员 ID（如已注册）
     */
    private Long memberId;

    /**
     * 设备类型：ios/android/h5
     */
    private String deviceType;

    /**
     * 扫码结果：1-新用户注册，2-老用户登录，3-游客访问，4-失败
     */
    private Integer scanResult;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
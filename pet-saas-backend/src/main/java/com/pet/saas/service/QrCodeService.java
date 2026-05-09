package com.pet.saas.service;

import com.pet.saas.dto.query.QrCodeStatQuery;
import com.pet.saas.dto.req.QrCodeBindReq;
import com.pet.saas.dto.resp.QrCodeStatVO;
import com.pet.saas.dto.resp.QrCodeVerifyVO;
import com.pet.saas.dto.resp.ShopQrCodeVO;

/**
 * 二维码服务接口
 */
public interface QrCodeService {

    /**
     * 获取店铺二维码（不存在则创建）
     *
     * @param tenantId 租户ID
     * @return 二维码信息
     */
    ShopQrCodeVO getOrCreateShopQrCode(Long tenantId);

    /**
     * 下载二维码图片
     *
     * @param qrCodeId 二维码ID
     * @return 二维码图片URL
     */
    String downloadQrCode(Long qrCodeId);

    /**
     * 刷新二维码
     *
     * @param qrCodeId 二维码ID
     * @return 新的二维码URL
     */
    String refreshQrCode(Long qrCodeId);

    /**
     * 获取二维码统计
     *
     * @param query 查询条件
     * @return 统计信息
     */
    QrCodeStatVO getQrCodeStat(QrCodeStatQuery query);

    /**
     * 验证二维码有效性
     *
     * @param scene 场景参数（如 tenant_123）
     * @return 验证结果
     */
    QrCodeVerifyVO verifyQrCode(String scene);

    /**
     * 扫码绑定会员到店铺
     *
     * @param req 绑定请求
     */
    void bindMember(QrCodeBindReq req);
}
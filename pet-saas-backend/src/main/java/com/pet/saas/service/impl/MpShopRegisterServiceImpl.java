package com.pet.saas.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.MpAuthTempData;
import com.pet.saas.dto.req.MpRegisterSubmitReq;
import com.pet.saas.dto.req.MpWxLoginReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.entity.ShopAdmin;
import com.pet.saas.entity.ShopConfig;
import com.pet.saas.entity.ShopWechatBind;
import com.pet.saas.entity.SysTenant;
import com.pet.saas.mapper.ShopAdminMapper;
import com.pet.saas.mapper.ShopConfigMapper;
import com.pet.saas.mapper.ShopWechatBindMapper;
import com.pet.saas.mapper.SysTenantMapper;
import com.pet.saas.service.MpShopRegisterService;
import com.pet.saas.service.MpWechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpShopRegisterServiceImpl implements MpShopRegisterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysTenantMapper sysTenantMapper;
    private final ShopAdminMapper shopAdminMapper;
    private final ShopConfigMapper shopConfigMapper;
    private final ShopWechatBindMapper shopWechatBindMapper;
    private final MpWechatService mpWechatService;

    /**
     * 微信授权临时token有效期（30分钟）
     */
    private static final long MP_TOKEN_EXPIRE_MINUTES = 30;

    @Override
    public String handleWxCallback(String code, String state) {
        log.info("处理微信公众号授权回调, code={}, state={}", code, state);

        // 1. 通过code获取微信用户信息
        MpWechatService.MpWechatUserInfo userInfo = mpWechatService.getUserInfo(code);

        // 2. 生成临时token
        String mpToken = RandomUtil.randomString(32);

        // 3. 存储临时数据到Redis
        MpAuthTempData tempData = MpAuthTempData.builder()
                .openid(userInfo.getOpenid())
                .unionid(userInfo.getUnionid())
                .nickname(userInfo.getNickname())
                .avatar(userInfo.getAvatar())
                .build();

        String redisKey = String.format(RedisKeyConstants.MP_TOKEN_KEY, mpToken);
        redisTemplate.opsForValue().set(redisKey, tempData, MP_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("微信授权临时token生成成功, mpToken={}, openid={}", mpToken, userInfo.getOpenid());
        return mpToken;
    }

    @Override
    public MpRegisterCheckRespVO checkRegister(String mpToken) {
        MpAuthTempData tempData = getTempData(mpToken);

        // 检查该微信是否已注册门店
        LambdaQueryWrapper<ShopWechatBind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopWechatBind::getOpenid, tempData.getOpenid());
        ShopWechatBind bind = shopWechatBindMapper.selectOne(wrapper);

        if (bind != null) {
            // 已注册
            return MpRegisterCheckRespVO.builder()
                    .registered(true)
                    .tenantId(bind.getTenantId())
                    .build();
        } else {
            // 未注册
            return MpRegisterCheckRespVO.builder()
                    .registered(false)
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MpRegisterSubmitRespVO submitRegister(MpRegisterSubmitReq req) {
        MpAuthTempData tempData = getTempData(req.getMpToken());

        // 确认微信未注册过
        LambdaQueryWrapper<ShopWechatBind> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(ShopWechatBind::getOpenid, tempData.getOpenid());
        if (shopWechatBindMapper.selectCount(bindWrapper) > 0) {
            throw new BusinessException(ErrorCode.WECHAT_ALREADY_REGISTERED);
        }

        // 获取用户填写的手机号
        String phone = req.getPhone();
        // 校验手机号唯一性（与Platform创建方式共用校验）
        LambdaQueryWrapper<SysTenant> tenantWrapper = new LambdaQueryWrapper<>();
        tenantWrapper.eq(SysTenant::getAdminPhone, phone);
        if (sysTenantMapper.selectCount(tenantWrapper) > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 1. 创建租户（status=1-启用，因为信息已完善）
        SysTenant tenant = new SysTenant();
        tenant.setShopName(req.getShopName());
        tenant.setAdminPhone(phone);
        tenant.setAddress(req.getAddress());
        tenant.setStatus(1);
        sysTenantMapper.insert(tenant);

        // 2. 生成账号密码
        String username;
        if (StrUtil.isNotBlank(phone)) {
            username = phone;
        } else {
            username = "mp_" + tempData.getOpenid();
        }
        String rawPassword = RandomUtil.randomString(8);

        // 3. 创建门店管理员
        createShopAdmin(tenant.getTenantId(), username, rawPassword);

        // 4. 创建门店配置
        ShopConfig config = new ShopConfig();
        config.setTenantId(tenant.getTenantId());
        config.setShopName(req.getShopName());
        config.setAddress(req.getAddress());
        config.setPhone(phone);
        shopConfigMapper.insert(config);

        // 5. 创建微信绑定关系
        ShopWechatBind bind = new ShopWechatBind();
        bind.setTenantId(tenant.getTenantId());
        bind.setOpenid(tempData.getOpenid());
        bind.setUnionid(tempData.getUnionid());
        bind.setNickname(tempData.getNickname());
        bind.setAvatar(tempData.getAvatar());
        shopWechatBindMapper.insert(bind);

        // 6. 发送公众号消息通知账号密码
        mpWechatService.sendRegisterSuccessNotification(
                tempData.getOpenid(),
                req.getShopName(),
                username,
                rawPassword
        );

        // 7. 生成登录token
        String token = doLogin(tenant.getTenantId(), username);

        // 8. 清理Redis临时数据
        String redisKey = String.format(RedisKeyConstants.MP_TOKEN_KEY, req.getMpToken());
        redisTemplate.delete(redisKey);

        log.info("门店注册成功, tenantId={}, shopName={}", tenant.getTenantId(), req.getShopName());

        return MpRegisterSubmitRespVO.builder()
                .tenantId(tenant.getTenantId())
                .token(token)
                .username(username)
                .password(rawPassword)
                .build();
    }

    @Override
    public MpShopLoginRespVO login(MpWxLoginReq req) {
        MpAuthTempData tempData = getTempData(req.getMpToken());

        // 查询微信绑定关系
        LambdaQueryWrapper<ShopWechatBind> bindWrapper = new LambdaQueryWrapper<>();
        bindWrapper.eq(ShopWechatBind::getOpenid, tempData.getOpenid());
        ShopWechatBind bind = shopWechatBindMapper.selectOne(bindWrapper);

        if (bind == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "未找到门店信息，请先注册");
        }

        // 检查租户状态
        SysTenant tenant = sysTenantMapper.selectById(bind.getTenantId());
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "租户不存在");
        }
        if (tenant.getStatus() == 0) {
            throw new BusinessException(ErrorCode.TENANT_DISABLED);
        }
        if (tenant.getStatus() == 2) {
            throw new BusinessException(ErrorCode.TENANT_INFO_INCOMPLETE);
        }

        // 查询门店管理员
        LambdaQueryWrapper<ShopAdmin> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(ShopAdmin::getTenantId, bind.getTenantId())
                .eq(ShopAdmin::getRole, "shop_admin")
                .last("LIMIT 1");
        ShopAdmin admin = shopAdminMapper.selectOne(adminWrapper);

        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "门店管理员不存在");
        }

        // 生成登录token
        String token = doLogin(bind.getTenantId(), admin.getUsername());

        log.info("微信公众号商家登录成功, tenantId={}", bind.getTenantId());

        return MpShopLoginRespVO.builder()
                .tenantId(bind.getTenantId())
                .token(token)
                .build();
    }

    /**
     * 从Redis获取临时数据
     */
    private MpAuthTempData getTempData(String mpToken) {
        String redisKey = String.format(RedisKeyConstants.MP_TOKEN_KEY, mpToken);
        Object data = redisTemplate.opsForValue().get(redisKey);
        if (data == null) {
            throw new BusinessException(ErrorCode.MP_TOKEN_INVALID);
        }
        return (MpAuthTempData) data;
    }

    /**
     * 保存临时数据到Redis（刷新过期时间）
     */
    private void saveTempData(String mpToken, MpAuthTempData tempData) {
        String redisKey = String.format(RedisKeyConstants.MP_TOKEN_KEY, mpToken);
        redisTemplate.opsForValue().set(redisKey, tempData, MP_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 创建门店管理员
     */
    private void createShopAdmin(Long tenantId, String username, String rawPassword) {
        ShopAdmin admin = new ShopAdmin();
        admin.setTenantId(tenantId);
        admin.setUsername(username);
        admin.setPassword(BCrypt.hashpw(rawPassword));
        admin.setRole("shop_admin");
        admin.setStatus(1);
        shopAdminMapper.insert(admin);
    }

    /**
     * 执行登录并返回token
     */
    private String doLogin(Long tenantId, String username) {
        // 查询门店管理员
        LambdaQueryWrapper<ShopAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopAdmin::getTenantId, tenantId)
                .eq(ShopAdmin::getUsername, username)
                .last("LIMIT 1");
        ShopAdmin admin = shopAdminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "门店管理员不存在");
        }

        StpLogic stpLogic = StpKit.SHOP;
        try {
            stpLogic.logout(admin.getId());
        } catch (Exception e) {
            log.debug("清理旧登录数据时忽略异常: {}", e.getMessage());
        }

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put(RedisKeyConstants.TENANT_ID_KEY, tenantId);
        sessionData.put("admin_id", admin.getId());
        sessionData.put("role", admin.getRole());

        stpLogic.login(admin.getId());
        sessionData.forEach((key, value) -> stpLogic.getSession().set(key, value));

        return stpLogic.getTokenValue();
    }
}

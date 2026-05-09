package com.pet.saas.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.dto.req.CreateTenantReq;
import com.pet.saas.dto.req.UpdateTenantStatusReq;
import com.pet.saas.dto.resp.CreateTenantRespVO;
import com.pet.saas.dto.query.TenantQuery;
import com.pet.saas.entity.ShopAdmin;
import com.pet.saas.entity.ShopConfig;
import com.pet.saas.entity.SysTenant;
import com.pet.saas.mapper.ShopAdminMapper;
import com.pet.saas.mapper.ShopConfigMapper;
import com.pet.saas.mapper.SysTenantMapper;
import com.pet.saas.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements TenantService {

    private final SysTenantMapper sysTenantMapper;
    private final ShopAdminMapper shopAdminMapper;
    private final ShopConfigMapper shopConfigMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateTenantRespVO createTenant(CreateTenantReq req) {
        // 1. 校验手机号唯一性：同一个手机只能创建一个租户
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getAdminPhone, req.getAdminPhone());
        if (sysTenantMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 2. 创建租户
        SysTenant tenant = new SysTenant();
        tenant.setShopName(req.getShopName());
        tenant.setAdminPhone(req.getAdminPhone());
        tenant.setAddress(req.getAddress());
        tenant.setStatus(1);
        sysTenantMapper.insert(tenant);

        // 3. 生成账号密码（用户名 = 手机号）
        String username = req.getAdminPhone();
        String rawPassword = RandomUtil.randomString(8);

        // 4. 创建门店管理员
        this.createShopAdmin(tenant.getTenantId(), username, rawPassword);

        // 5. 创建门店配置
        ShopConfig config = new ShopConfig();
        config.setTenantId(tenant.getTenantId());
        config.setShopName(req.getShopName());
        config.setAddress(req.getAddress());
        shopConfigMapper.insert(config);

        // 6. 发送短信通知（预留接口）
        this.sendSmsNotification(req.getAdminPhone(), rawPassword);

        return CreateTenantRespVO.builder()
                .tenantId(tenant.getTenantId())
                .username(username)
                .build();
    }

    /**
     * 发送短信通知（预留接口）
     *  TODO: 后续接入短信服务商后实现
     *
     * @param phone 手机号
     * @param password 初始密码
     */
    private void sendSmsNotification(String phone, String password) {
        log.info("========== 短信发送预留接口 ==========");
        log.info("手机号: {}", phone);
        log.info("初始密码: {}", password);
        log.info("请接入短信服务商后实现真实发送逻辑");
    }

    @Override
    public Page<SysTenant> listTenants(TenantQuery query) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getShopName())) {
            wrapper.like(SysTenant::getShopName, query.getShopName());
        }
        if (StrUtil.isNotBlank(query.getAdminPhone())) {
            wrapper.eq(SysTenant::getAdminPhone, query.getAdminPhone());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysTenant::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(SysTenant::getCreateTime);
        return sysTenantMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTenantStatus(UpdateTenantStatusReq req) {
        SysTenant tenant = sysTenantMapper.selectById(req.getTenantId());
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "租户不存在");
        }
        if (req.getStatus() != null) {
            tenant.setStatus(req.getStatus());
            sysTenantMapper.updateById(tenant);
        }
        if (Boolean.TRUE.equals(req.getResetPassword())) {
            this.resetShopAdminPassword(tenant);
        }
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
     * 重置门店管理员密码
     */
    private void resetShopAdminPassword(SysTenant tenant) {
        LambdaQueryWrapper<ShopAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopAdmin::getTenantId, tenant.getTenantId())
                .eq(ShopAdmin::getRole, "shop_admin")
                .last("LIMIT 1");
        ShopAdmin admin = shopAdminMapper.selectOne(wrapper);
        if (admin != null) {
            String rawPassword = RandomUtil.randomString(8);
            log.info("rawPassword = {}", rawPassword);
            admin.setPassword(BCrypt.hashpw(rawPassword));
            shopAdminMapper.updateById(admin);
            this.sendSmsNotification(tenant.getAdminPhone(), rawPassword);
        }
    }
}

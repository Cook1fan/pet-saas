package com.pet.saas.service.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.pet.saas.common.util.StpKit;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.dto.req.MerchantLoginReq;
import com.pet.saas.dto.req.PcLoginReq;
import com.pet.saas.dto.req.PlatformLoginReq;
import com.pet.saas.dto.req.WxLoginReq;
import com.pet.saas.dto.resp.LoginRespVO;
import com.pet.saas.dto.resp.MemberVO;
import com.pet.saas.dto.resp.PlatformAdminVO;
import com.pet.saas.dto.resp.ShopAdminVO;
import com.pet.saas.dto.resp.WxLoginRespVO;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ShopAdminMapper shopAdminMapper;
    private final SysTenantMapper sysTenantMapper;
    private final MemberMapper memberMapper;
    private final MemberAccountMapper memberAccountMapper;
    private final MemberShopBindMapper memberShopBindMapper;
    private final SysPlatformAdminMapper sysPlatformAdminMapper;

    @Override
    public LoginRespVO pcLogin(PcLoginReq req) {
        return shopAdminLogin(req.getUsername(), req.getPassword(), "pc");
    }

    @Override
    public LoginRespVO merchantLogin(MerchantLoginReq req) {
        return shopAdminLogin(req.getUsername(), req.getPassword(), "merchant");
    }

    private LoginRespVO shopAdminLogin(String username, String password, String loginType) {
        // 先根据用户名查询管理员（可能有多个租户有相同用户名，取第一个）
        LambdaQueryWrapper<ShopAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopAdmin::getUsername, username)
                .last("LIMIT 1");
        ShopAdmin admin = shopAdminMapper.selectOne(wrapper);

        validateAdmin(admin, password);

        // 校验租户状态
        SysTenant tenant = sysTenantMapper.selectById(admin.getTenantId());
        if (tenant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "租户不存在");
        }
        if (tenant.getStatus() == 0) {
            throw new BusinessException(ErrorCode.TENANT_DISABLED);
        }

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put(RedisKeyConstants.TENANT_ID_KEY, admin.getTenantId());
        sessionData.put("admin_id", admin.getId());
        sessionData.put("role", admin.getRole());

        ShopAdminVO adminVO = BeanConverter.convert(admin, ShopAdminVO.class);
        return doLogin(admin.getId(), StpKit.SHOP, sessionData, adminVO);
    }

    @Override
    public LoginRespVO platformLogin(PlatformLoginReq req) {
        LambdaQueryWrapper<SysPlatformAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPlatformAdmin::getUsername, req.getUsername());
        SysPlatformAdmin admin = sysPlatformAdminMapper.selectOne(wrapper);

        if (admin == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }
        if (!BCrypt.checkpw(req.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("admin_id", admin.getId());
        sessionData.put("role", admin.getRole());

        PlatformAdminVO adminVO = BeanConverter.convert(admin, PlatformAdminVO.class);
        return doLogin(admin.getId(), StpKit.PLATFORM, sessionData, adminVO);
    }

    private void validateAdmin(ShopAdmin admin, String password) {
        if (admin == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR);
        }
        if (admin.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR.getCode(), "账号已被禁用");
        }
    }

    private <T> LoginRespVO doLogin(Object loginId, StpLogic stpLogic, Map<String, Object> sessionData, T userVO) {
        try {
            stpLogic.logout(loginId);
        } catch (Exception e) {
            log.debug("清理旧登录数据时忽略异常: {}", e.getMessage());
        }

        stpLogic.login(loginId);
        if (sessionData != null) {
            sessionData.forEach((key, value) -> stpLogic.getSession().set(key, value));
        }

        LoginRespVO result = new LoginRespVO();
        result.setToken(stpLogic.getTokenValue());
        result.setUser(userVO);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginRespVO wxLogin(WxLoginReq req) {
        String openid = "mock_openid_" + req.getCode();

        Long tenantId = req.getTenantId();
        boolean isNewMember = false;

        Member member = null;
        if (tenantId != null) {
            LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Member::getTenantId, tenantId)
                    .eq(Member::getOpenid, openid);
            member = memberMapper.selectOne(wrapper);

            if (member == null) {
                member = new Member();
                member.setTenantId(tenantId);
                member.setOpenid(openid);
                member.setName("微信用户");
                member.setPhone("");
                memberMapper.insert(member);

                MemberAccount account = new MemberAccount();
                account.setTenantId(tenantId);
                account.setMemberId(member.getId());
                account.setBalance(BigDecimal.ZERO);
                account.setTotalRecharge(BigDecimal.ZERO);
                memberAccountMapper.insert(account);

                isNewMember = true;
            }

            LambdaQueryWrapper<MemberShopBind> bindWrapper = new LambdaQueryWrapper<>();
            bindWrapper.eq(MemberShopBind::getOpenid, openid)
                    .eq(MemberShopBind::getTenantId, tenantId);
            MemberShopBind bind = memberShopBindMapper.selectOne(bindWrapper);
            if (bind == null) {
                bind = new MemberShopBind();
                bind.setOpenid(openid);
                bind.setTenantId(tenantId);
                bind.setMemberId(member.getId());
                bind.setLastVisitTime(LocalDateTime.now());
                memberShopBindMapper.insert(bind);
            } else {
                bind.setLastVisitTime(LocalDateTime.now());
                memberShopBindMapper.updateById(bind);
            }
        } else {
            // 没有 tenantId，查找该 openid 最近访问过的店铺
            LambdaQueryWrapper<MemberShopBind> bindWrapper = new LambdaQueryWrapper<>();
            bindWrapper.eq(MemberShopBind::getOpenid, openid)
                    .orderByDesc(MemberShopBind::getLastVisitTime)
                    .last("LIMIT 1");
            MemberShopBind bind = memberShopBindMapper.selectOne(bindWrapper);
            if (bind != null) {
                // 找到历史记录，使用该店铺
                tenantId = bind.getTenantId();
                member = memberMapper.selectById(bind.getMemberId());
            } else {
                // 首次访问，没有任何记录
                // 创建一个临时会员（未绑定任何店铺）
                member = new Member();
                member.setOpenid(openid);
                member.setName("微信用户");
                member.setPhone("");
                member.setTenantId(null); // 暂时没有店铺
                memberMapper.insert(member);
                isNewMember = true;
            }
        }

        WxLoginRespVO result = new WxLoginRespVO();
        result.setOpenid(openid);
        result.setIsNewMember(isNewMember);

        // 无论是否有 tenantId，都创建 token
        // 如果没有店铺，tenantId 为 null，但可以用于后续绑定店铺
        if (member != null) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("member_id", member.getId());
            sessionData.put("openid", openid);
            if (tenantId != null) {
                sessionData.put(RedisKeyConstants.TENANT_ID_KEY, tenantId);
            }

            MemberVO memberVO = BeanConverter.convert(member, MemberVO.class);
            LoginRespVO loginResp = doLogin(member.getId(), StpKit.MEMBER, sessionData, memberVO);
            result.setToken(loginResp.getToken());
            result.setMember(memberVO);
        }

        return result;
    }

    @Override
    public void pcLogout() {
        StpKit.SHOP.logout();
    }

    @Override
    public void platformLogout() {
        StpKit.PLATFORM.logout();
    }

    @Override
    public void merchantLogout() {
        StpKit.SHOP.logout();
    }

    @Override
    public void userLogout() {
        StpKit.MEMBER.logout();
    }
}

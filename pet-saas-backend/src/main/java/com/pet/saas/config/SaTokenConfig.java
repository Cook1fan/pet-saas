package com.pet.saas.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import com.pet.saas.common.util.StpKit;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 门店端接口：需要 SHOP 账号登录
                    SaRouter.match("/api/pc/**", "/api/merchant/**", r -> StpKit.SHOP.checkLogin());

                    // 平台端接口：需要 PLATFORM 账号登录
                    SaRouter.match("/api/platform/**", r -> StpKit.PLATFORM.checkLogin());

                    // 用户端接口：需要 MEMBER 账号登录
                    SaRouter.match("/api/user/**", r -> StpKit.MEMBER.checkLogin());
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/pc/login",
                        "/api/pc/logout",
                        "/api/merchant/login",
                        "/api/merchant/logout",
                        "/api/user/wxLogin",
                        "/api/user/logout",
                        "/api/platform/login",
                        "/api/platform/logout",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );
    }
}

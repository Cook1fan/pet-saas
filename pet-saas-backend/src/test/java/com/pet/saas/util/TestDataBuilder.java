package com.pet.saas.util;

import com.pet.saas.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataBuilder {

    public static MemberBuilder member() {
        return new MemberBuilder();
    }

    public static OrderBuilder order() {
        return new OrderBuilder();
    }

    public static GoodsBuilder goods() {
        return new GoodsBuilder();
    }

    public static MemberAccountBuilder memberAccount() {
        return new MemberAccountBuilder();
    }

    public static class MemberBuilder {
        private Long id = 1L;
        private Long tenantId = 1L;
        private String phone = "13800138000";
        private String name = "测试会员";
        private String openid = "test_openid_001";

        public MemberBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MemberBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public MemberBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public MemberBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Member build() {
            Member member = new Member();
            member.setId(id);
            member.setTenantId(tenantId);
            member.setPhone(phone);
            member.setName(name);
            member.setOpenid(openid);
            member.setCreateTime(LocalDateTime.now());
            return member;
        }
    }

    public static class GoodsBuilder {
        private Long id = 1L;
        private Long tenantId = 1L;
        private String goodsName = "测试商品";
        private Long categoryId = 1L;
        private Integer isService = 0;
        private Integer status = 1;

        public GoodsBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public GoodsBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public GoodsBuilder goodsName(String goodsName) {
            this.goodsName = goodsName;
            return this;
        }

        public GoodsBuilder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public GoodsBuilder isService(Integer isService) {
            this.isService = isService;
            return this;
        }

        public Goods build() {
            Goods goods = new Goods();
            goods.setId(id);
            goods.setTenantId(tenantId);
            goods.setGoodsName(goodsName);
            goods.setCategoryId(categoryId);
            goods.setIsService(isService);
            goods.setStatus(status);
            goods.setCreateTime(LocalDateTime.now());
            goods.setUpdateTime(LocalDateTime.now());
            return goods;
        }
    }

    public static class OrderBuilder {
        private Long id = 1L;
        private Long tenantId = 1L;
        private String orderNo = "TEST" + System.currentTimeMillis();
        private Long memberId = 1L;
        private BigDecimal totalAmount = new BigDecimal("99.00");
        private BigDecimal payAmount = new BigDecimal("99.00");
        private Integer payType = 1;
        private Integer payStatus = 0;

        public OrderBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public OrderBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public OrderBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public OrderBuilder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public OrderBuilder payType(Integer payType) {
            this.payType = payType;
            return this;
        }

        public OrderBuilder payStatus(Integer payStatus) {
            this.payStatus = payStatus;
            return this;
        }

        public OrderInfo build() {
            OrderInfo order = new OrderInfo();
            order.setId(id);
            order.setTenantId(tenantId);
            order.setOrderNo(orderNo);
            order.setMemberId(memberId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(payAmount);
            order.setPayType(payType);
            order.setPayStatus(payStatus);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            return order;
        }
    }

    public static class MemberAccountBuilder {
        private Long id = 1L;
        private Long tenantId = 1L;
        private Long memberId = 1L;
        private BigDecimal balance = new BigDecimal("1000.00");
        private BigDecimal totalRecharge = new BigDecimal("1000.00");

        public MemberAccountBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MemberAccountBuilder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public MemberAccountBuilder memberId(Long memberId) {
            this.memberId = memberId;
            return this;
        }

        public MemberAccountBuilder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public MemberAccount build() {
            MemberAccount account = new MemberAccount();
            account.setId(id);
            account.setTenantId(tenantId);
            account.setMemberId(memberId);
            account.setBalance(balance);
            account.setTotalRecharge(totalRecharge);
            account.setCreateTime(LocalDateTime.now());
            account.setUpdateTime(LocalDateTime.now());
            return account;
        }
    }
}

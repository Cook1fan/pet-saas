package com.pet.saas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.MemberAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface MemberAccountMapper extends BaseMapper<MemberAccount> {

    @Update("UPDATE member_account SET balance = balance - #{amount} WHERE member_id = #{memberId} AND balance >= #{amount}")
    int deductBalance(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);

    @Update("UPDATE member_account SET balance = balance + #{amount}, total_recharge = total_recharge + #{amount} WHERE member_id = #{memberId}")
    int addBalance(@Param("memberId") Long memberId, @Param("amount") BigDecimal amount);
}

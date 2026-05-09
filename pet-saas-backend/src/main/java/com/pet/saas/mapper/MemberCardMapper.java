package com.pet.saas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.MemberCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MemberCardMapper extends BaseMapper<MemberCard> {

    @Update("UPDATE member_card SET remain_times = remain_times - 1 WHERE id = #{cardId} AND remain_times >= 1 AND expire_time > NOW()")
    int deductTimes(@Param("cardId") Long cardId);
}

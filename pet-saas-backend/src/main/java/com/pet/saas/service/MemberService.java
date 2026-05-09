package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.MemberQuery;
import com.pet.saas.dto.req.PcMemberCreateReq;
import com.pet.saas.dto.req.PcMemberUpdateReq;
import com.pet.saas.entity.Member;
import com.pet.saas.entity.MemberAccount;
import com.pet.saas.entity.PetInfo;

import java.util.List;

public interface MemberService {

    Page<Member> listMembers(MemberQuery query, Long tenantId);

    Member createMember(PcMemberCreateReq req, Long tenantId);

    Member updateMember(PcMemberUpdateReq req, Long tenantId);

    MemberAccount getMemberAccount(Long tenantId, Long memberId);

    List<PetInfo> listPets(Long tenantId, Long memberId);

    void savePet(Long tenantId, PetInfo pet);
}

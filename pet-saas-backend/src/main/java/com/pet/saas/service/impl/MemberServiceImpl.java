package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.dto.req.PcMemberCreateReq;
import com.pet.saas.dto.req.PcMemberUpdateReq;
import com.pet.saas.dto.query.MemberQuery;
import com.pet.saas.entity.Member;
import com.pet.saas.entity.MemberAccount;
import com.pet.saas.entity.PetInfo;
import com.pet.saas.mapper.MemberAccountMapper;
import com.pet.saas.mapper.MemberMapper;
import com.pet.saas.mapper.PetInfoMapper;
import com.pet.saas.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final MemberMapper memberMapper;
    private final MemberAccountMapper memberAccountMapper;
    private final PetInfoMapper petInfoMapper;

    @Override
    public Page<Member> listMembers(MemberQuery query, Long tenantId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Member::getPhone, query.getKeyword())
                    .or().like(Member::getName, query.getKeyword()));
        }
        if (query.getTag() != null && !query.getTag().isEmpty()) {
            wrapper.like(Member::getTags, query.getTag());
        }
        wrapper.orderByDesc(Member::getCreateTime);
        return memberMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member createMember(PcMemberCreateReq req, Long tenantId) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getPhone, req.getPhone());
        if (memberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该会员已存在");
        }

        Member member = new Member();
        member.setTenantId(tenantId);
        member.setPhone(req.getPhone());
        member.setName(req.getName());
        member.setTags(req.getTags());
        memberMapper.insert(member);

        MemberAccount account = new MemberAccount();
        account.setTenantId(tenantId);
        account.setMemberId(member.getId());
        account.setBalance(BigDecimal.ZERO);
        account.setTotalRecharge(BigDecimal.ZERO);
        memberAccountMapper.insert(account);

        if (req.getPets() != null && !req.getPets().isEmpty()) {
            for (PetInfo pet : req.getPets()) {
                pet.setTenantId(tenantId);
                pet.setMemberId(member.getId());
                petInfoMapper.insert(pet);
            }
        }

        return memberMapper.selectById(member.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member updateMember(PcMemberUpdateReq req, Long tenantId) {
        Member member = memberMapper.selectById(req.getId());
        if (member == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "会员不存在");
        }

        if (req.getPhone() != null && !req.getPhone().equals(member.getPhone())) {
            LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Member::getPhone, req.getPhone())
                    .ne(Member::getId, req.getId());
            if (memberMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该手机号已被使用");
            }
        }

        if (req.getName() != null) {
            member.setName(req.getName());
        }
        if (req.getPhone() != null) {
            member.setPhone(req.getPhone());
        }
        if (req.getTags() != null) {
            member.setTags(req.getTags());
        }
        memberMapper.updateById(member);

        if (req.getPets() != null) {
            List<Long> requestPetIds = req.getPets().stream()
                    .map(PetInfo::getId)
                    .filter(Objects::nonNull)
                    .toList();

            List<PetInfo> existingPets = petInfoMapper.selectList(
                    new LambdaQueryWrapper<PetInfo>()
                            .eq(PetInfo::getMemberId, req.getId())
            );

            List<Long> toDeleteIds = existingPets.stream()
                    .map(PetInfo::getId)
                    .filter(id -> !requestPetIds.contains(id))
                    .collect(Collectors.toList());

            if (!toDeleteIds.isEmpty()) {
                petInfoMapper.deleteBatchIds(toDeleteIds);
            }

            for (PetInfo pet : req.getPets()) {
                pet.setTenantId(tenantId);
                pet.setMemberId(req.getId());
                if (pet.getId() == null) {
                    petInfoMapper.insert(pet);
                } else {
                    petInfoMapper.updateById(pet);
                }
            }
        }

        return memberMapper.selectById(member.getId());
    }

    @Override
    public MemberAccount getMemberAccount(Long tenantId, Long memberId) {
        return memberAccountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>()
                        .eq(MemberAccount::getMemberId, memberId));
    }

    @Override
    public List<PetInfo> listPets(Long tenantId, Long memberId) {
        return petInfoMapper.selectList(
                new LambdaQueryWrapper<PetInfo>()
                        .eq(PetInfo::getMemberId, memberId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePet(Long tenantId, PetInfo pet) {
        pet.setTenantId(tenantId);
        if (pet.getId() == null) {
            petInfoMapper.insert(pet);
        } else {
            petInfoMapper.updateById(pet);
        }
    }
}

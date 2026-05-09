package com.pet.saas.dto.req;

import lombok.Data;

/**
 * 拼团活动查询请求 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
public class GroupActivityQueryReq {

    private Integer status;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}

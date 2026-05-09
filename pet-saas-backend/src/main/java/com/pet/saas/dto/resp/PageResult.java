package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页响应")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "总页数")
    private long pages;
}

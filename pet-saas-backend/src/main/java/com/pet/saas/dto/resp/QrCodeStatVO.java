package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 二维码统计响应 VO
 */
@Data
@Schema(description = "二维码统计响应")
public class QrCodeStatVO {

    @Schema(description = "总扫码次数")
    private Integer totalScan;

    @Schema(description = "新用户数")
    private Integer newUser;

    @Schema(description = "老用户数")
    private Integer oldUser;

    @Schema(description = "游客访问数")
    private Integer guestUser;

    @Schema(description = "每日统计")
    private List<DailyStat> dailyStats;

    @Data
    @Schema(description = "每日统计")
    public static class DailyStat {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "扫码次数")
        private Integer scanCount;

        @Schema(description = "新用户数")
        private Integer newUser;
    }
}
package com.pet.saas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.QrScanLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二维码扫码日志 Mapper
 */
@Mapper
public interface QrScanLogMapper extends BaseMapper<QrScanLog> {
}
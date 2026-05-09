-- =============================================
-- 拼团模块数据库迁移脚本
-- =============================================

-- 1. 创建拼团组表
CREATE TABLE IF NOT EXISTS activity_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    group_no VARCHAR(32) NOT NULL COMMENT '拼团组号（唯一）',
    leader_member_id BIGINT NOT NULL COMMENT '团长会员ID',
    current_num INT NOT NULL DEFAULT 1 COMMENT '当前人数',
    target_num INT NOT NULL COMMENT '目标成团人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-拼团中，2-拼团成功，3-拼团失败',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    success_time DATETIME COMMENT '成团时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_group_no (group_no),
    INDEX idx_tenant_activity (tenant_id, activity_id),
    INDEX idx_status_expire (status, expire_time),
    INDEX idx_leader_member (leader_member_id)
) COMMENT '拼团组表';

-- 2. 给 activity_info 表添加 sku_id 字段
ALTER TABLE activity_info
ADD COLUMN IF NOT EXISTS sku_id BIGINT COMMENT 'SKU ID' AFTER goods_id;

-- 3. 给 activity_info 表添加 group_valid_hours 字段
ALTER TABLE activity_info
ADD COLUMN IF NOT EXISTS group_valid_hours INT NOT NULL DEFAULT 24 COMMENT '拼团有效期（小时）' AFTER group_count;

-- 4. 确保 activity_order 表的 group_id 索引存在
ALTER TABLE activity_order
ADD INDEX IF NOT EXISTS idx_group_id (group_id);

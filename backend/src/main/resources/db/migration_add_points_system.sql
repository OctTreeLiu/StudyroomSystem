-- 数据库迁移脚本：添加积分系统相关表结构
-- 执行此脚本前，请确保已经创建了 user 表

USE studyroom_db;

-- 1. 为用户表添加总积分字段
ALTER TABLE `user` 
ADD COLUMN `total_points` INT DEFAULT 0 NOT NULL COMMENT '用户总积分' AFTER `signature`;

-- 2. 创建积分流水表
CREATE TABLE IF NOT EXISTS `points` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `points` INT NOT NULL COMMENT '积分数量（正数表示增加，负数表示减少）',
    `type` VARCHAR(20) NOT NULL COMMENT '积分类型：签到/预约奖励/积分兑换/会员赠送/管理员调整',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '积分描述',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID（预约订单ID、会员订单ID等）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_type` (`type`),
    CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水记录表';


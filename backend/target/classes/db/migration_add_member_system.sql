-- 数据库迁移脚本：添加会员系统相关表结构
-- 执行此脚本前，请确保已经创建了 user 表

USE studyroom_db;

-- 1. 为用户表添加会员相关字段
ALTER TABLE `user` 
ADD COLUMN `member_type` TINYINT DEFAULT 0 NOT NULL COMMENT '会员类型：0-普通用户，1-VIP，2-SVIP' AFTER `total_points`,
ADD COLUMN `member_expire_time` DATETIME DEFAULT NULL COMMENT '会员到期时间' AFTER `member_type`;

-- 2. 创建会员订单表
CREATE TABLE IF NOT EXISTS `member_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `member_type` TINYINT NOT NULL COMMENT '会员类型：1-VIP，2-SVIP',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    `payment_status` TINYINT DEFAULT 0 NOT NULL COMMENT '付款状态：0-未付款，1-已付款',
    `payment_time` DATETIME DEFAULT NULL COMMENT '付款时间',
    `start_date` DATE NOT NULL COMMENT '会员开始日期',
    `end_date` DATE NOT NULL COMMENT '会员结束日期',
    `points_awarded` INT DEFAULT 0 NOT NULL COMMENT '赠送的积分数量',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_number` (`order_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_member_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单表';


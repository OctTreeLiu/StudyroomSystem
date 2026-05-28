-- 数据库迁移脚本：添加额外收费订单表
-- 执行此脚本前，请确保已经创建了 user 表

USE studyroom_db;

-- 创建额外收费订单表
CREATE TABLE IF NOT EXISTS `extra_charge_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_number` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '收费金额',
    `content` VARCHAR(500) NOT NULL COMMENT '收费内容',
    `payment_status` TINYINT DEFAULT 0 NOT NULL COMMENT '付款状态：0-未付款，1-已付款，2-已取消',
    `payment_time` DATETIME DEFAULT NULL COMMENT '付款时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_number` (`order_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_payment_status` (`payment_status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_extra_charge_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='额外收费订单表';


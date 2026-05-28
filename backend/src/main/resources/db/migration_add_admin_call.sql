-- 数据库迁移脚本：创建管理员呼叫记录表
USE studyroom_db;

-- 创建管理员呼叫记录表
CREATE TABLE IF NOT EXISTS `admin_call` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '呼叫ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '呼叫用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '呼叫用户名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '用户联系电话',
    `seat_info` VARCHAR(200) DEFAULT NULL COMMENT '座位信息（如：A区-12号座位）',
    `message` VARCHAR(500) DEFAULT NULL COMMENT '用户留言',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-已处理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '呼叫时间',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `handle_admin_id` BIGINT(20) DEFAULT NULL COMMENT '处理管理员ID',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_admin_call_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员呼叫记录表';


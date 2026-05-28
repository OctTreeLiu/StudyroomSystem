-- 数据库迁移脚本：添加用户修改日志表
-- 执行此脚本前，请确保已经创建了 user 表

USE studyroom_db;

-- 创建用户修改日志表
CREATE TABLE IF NOT EXISTS `user_modify_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '被修改的用户ID',
    `admin_id` BIGINT NOT NULL COMMENT '操作的管理员ID',
    `admin_username` VARCHAR(50) NOT NULL COMMENT '操作的管理员用户名',
    `user_username` VARCHAR(50) NOT NULL COMMENT '被修改的用户名',
    `before_member_type` TINYINT DEFAULT NULL COMMENT '修改前的会员类型：0-普通用户，1-VIP，2-SVIP',
    `after_member_type` TINYINT DEFAULT NULL COMMENT '修改后的会员类型：0-普通用户，1-VIP，2-SVIP',
    `before_member_expire_time` DATETIME DEFAULT NULL COMMENT '修改前的会员到期时间',
    `after_member_expire_time` DATETIME DEFAULT NULL COMMENT '修改后的会员到期时间',
    `modify_detail` TEXT DEFAULT NULL COMMENT '修改详情（JSON格式，存储修改前后的完整信息）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_admin_id` (`admin_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_user_modify_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_modify_log_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户修改日志表';


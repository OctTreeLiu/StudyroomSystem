-- 留言板表迁移脚本
-- 执行此脚本以添加留言板功能

USE studyroom_db;

-- 留言板表
DROP TABLE IF EXISTS `message_board`;
CREATE TABLE `message_board` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '留言ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '留言内容',
    `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-不匿名，1-匿名',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_message_board_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言板表';


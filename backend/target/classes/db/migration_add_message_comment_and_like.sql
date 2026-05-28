-- 留言板评论和点赞功能迁移脚本
-- 执行此脚本以添加评论和点赞功能

USE studyroom_db;

-- 留言评论表
DROP TABLE IF EXISTS `message_comment`;
CREATE TABLE `message_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `message_id` BIGINT(20) NOT NULL COMMENT '留言ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '评论用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `is_anonymous` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匿名：0-不匿名，1-匿名',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_message_comment_message` FOREIGN KEY (`message_id`) REFERENCES `message_board` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_message_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言评论表';

-- 留言点赞表
DROP TABLE IF EXISTS `message_like`;
CREATE TABLE `message_like` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `message_id` BIGINT(20) NOT NULL COMMENT '留言ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '点赞用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_user` (`message_id`, `user_id`),
    KEY `idx_message_id` (`message_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_message_like_message` FOREIGN KEY (`message_id`) REFERENCES `message_board` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_message_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言点赞表';


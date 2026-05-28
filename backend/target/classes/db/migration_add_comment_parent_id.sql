-- 添加次级评论功能迁移脚本
-- 为 message_comment 表添加 parent_id 字段，支持评论的评论（二级评论结构）

USE studyroom_db;

-- 为 message_comment 表添加 parent_id 字段
ALTER TABLE `message_comment` 
ADD COLUMN `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父评论ID（NULL表示一级评论，有值表示次级评论）' AFTER `message_id`,
ADD KEY `idx_parent_id` (`parent_id`),
ADD CONSTRAINT `fk_message_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `message_comment` (`id`) ON DELETE CASCADE;


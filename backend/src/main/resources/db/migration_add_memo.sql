-- 备忘录表
DROP TABLE IF EXISTS `memo`;
CREATE TABLE `memo` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '备忘录ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '主题',
    `content` TEXT COMMENT '内容',
    `memo_date` DATE NOT NULL COMMENT '日期',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-未处理，1-已处理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_memo_date` (`memo_date`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_memo_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备忘录表';


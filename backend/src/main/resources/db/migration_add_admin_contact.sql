-- 数据库迁移脚本：创建管理员联系方式配置表
USE studyroom_db;

-- 创建管理员联系方式配置表
CREATE TABLE IF NOT EXISTS `admin_contact` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员联系方式配置表';

-- 插入默认数据（如果表为空）
INSERT INTO `admin_contact` (`phone`, `email`) 
SELECT '12345678901', '1234567890@qq.com'
WHERE NOT EXISTS (SELECT 1 FROM `admin_contact`);


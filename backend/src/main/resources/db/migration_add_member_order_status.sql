-- 数据库迁移脚本：为会员订单表添加状态字段
-- 执行此脚本前，请确保已经创建了 member_order 表

USE studyroom_db;

-- 为会员订单表添加状态字段
ALTER TABLE `member_order` 
ADD COLUMN `status` TINYINT DEFAULT 0 NOT NULL COMMENT '订单状态：0-待支付，1-已支付，2-已取消' AFTER `payment_status`;

-- 更新现有数据：根据payment_status设置status
UPDATE `member_order` SET `status` = 1 WHERE `payment_status` = 1;
UPDATE `member_order` SET `status` = 0 WHERE `payment_status` = 0;


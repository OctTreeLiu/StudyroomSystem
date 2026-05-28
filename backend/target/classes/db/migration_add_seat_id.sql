-- 数据库迁移脚本：为现有表添加 seat_id 字段
-- 执行此脚本前，请确保已经创建了 seat 表并插入了座位数据

USE studyroom_db;

-- 为预约表添加 seat_id 字段
ALTER TABLE `reservation` 
ADD COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID' AFTER `room_id`,
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_reservation_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- 注意：由于现有预约记录没有座位信息，需要先处理现有数据
-- 方案1：删除所有现有预约记录（如果允许）
-- DELETE FROM reservation;

-- 方案2：为现有预约记录分配默认座位（需要根据实际情况调整）
-- 这里假设为每个预约分配对应自习室的第一个座位
-- UPDATE reservation r
-- SET r.seat_id = (
--     SELECT s.id 
--     FROM seat s 
--     WHERE s.room_id = r.room_id 
--     ORDER BY s.seat_number ASC 
--     LIMIT 1
-- );

-- 为长期租赁表添加 seat_id 字段
ALTER TABLE `long_term_lease` 
ADD COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID' AFTER `room_id`,
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_lease_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- 注意：由于现有长期租赁记录没有座位信息，需要先处理现有数据
-- 方案1：删除所有现有长期租赁记录（如果允许）
-- DELETE FROM long_term_lease;

-- 方案2：为现有长期租赁记录分配默认座位
-- UPDATE long_term_lease l
-- SET l.seat_id = (
--     SELECT s.id 
--     FROM seat s 
--     WHERE s.room_id = l.room_id 
--     ORDER BY s.seat_number ASC 
--     LIMIT 1
-- );

-- 为使用记录表添加 seat_id 字段
ALTER TABLE `usage_record` 
ADD COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID' AFTER `room_id`,
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_record_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- 注意：由于现有使用记录没有座位信息，需要先处理现有数据
-- 方案1：删除所有现有使用记录（如果允许）
-- DELETE FROM usage_record;

-- 方案2：为现有使用记录分配默认座位
-- UPDATE usage_record ur
-- SET ur.seat_id = (
--     SELECT s.id 
--     FROM seat s 
--     WHERE s.room_id = ur.room_id 
--     ORDER BY s.seat_number ASC 
--     LIMIT 1
-- );


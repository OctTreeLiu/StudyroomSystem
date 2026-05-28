-- 数据库迁移脚本：为现有表添加 seat_id 字段（安全版本，自动处理现有数据）
-- 执行此脚本前，请确保已经创建了 seat 表并插入了座位数据

USE studyroom_db;

-- 为预约表添加 seat_id 字段（允许为空，先添加）
ALTER TABLE `reservation` 
ADD COLUMN `seat_id` BIGINT(20) NULL COMMENT '座位ID' AFTER `room_id`;

-- 为现有预约记录分配默认座位（每个预约分配对应自习室的第一个座位）
UPDATE reservation r
SET r.seat_id = (
    SELECT s.id 
    FROM seat s 
    WHERE s.room_id = r.room_id 
    ORDER BY s.seat_number ASC 
    LIMIT 1
)
WHERE r.seat_id IS NULL;

-- 将 seat_id 设置为 NOT NULL
ALTER TABLE `reservation` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_reservation_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- 为长期租赁表添加 seat_id 字段（允许为空，先添加）
ALTER TABLE `long_term_lease` 
ADD COLUMN `seat_id` BIGINT(20) NULL COMMENT '座位ID' AFTER `room_id`;

-- 为现有长期租赁记录分配默认座位
UPDATE long_term_lease l
SET l.seat_id = (
    SELECT s.id 
    FROM seat s 
    WHERE s.room_id = l.room_id 
    ORDER BY s.seat_number ASC 
    LIMIT 1
)
WHERE l.seat_id IS NULL;

-- 将 seat_id 设置为 NOT NULL
ALTER TABLE `long_term_lease` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_lease_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- 为使用记录表添加 seat_id 字段（允许为空，先添加）
ALTER TABLE `usage_record` 
ADD COLUMN `seat_id` BIGINT(20) NULL COMMENT '座位ID' AFTER `room_id`;

-- 为现有使用记录分配默认座位
-- 优先从关联的预约或租赁记录中获取座位ID
UPDATE usage_record ur
SET ur.seat_id = (
    SELECT COALESCE(
        (SELECT r.seat_id FROM reservation r WHERE r.id = ur.reservation_id),
        (SELECT l.seat_id FROM long_term_lease l WHERE l.id = ur.lease_id),
        (SELECT s.id FROM seat s WHERE s.room_id = ur.room_id ORDER BY s.seat_number ASC LIMIT 1)
    )
)
WHERE ur.seat_id IS NULL;

-- 将 seat_id 设置为 NOT NULL
ALTER TABLE `usage_record` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_record_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);


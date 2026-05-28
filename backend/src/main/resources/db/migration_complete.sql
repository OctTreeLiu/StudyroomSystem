-- 完整的数据库迁移脚本：创建座位表并添加 seat_id 字段
-- 此脚本会先创建 seat 表，然后为现有表添加 seat_id 字段

USE studyroom_db;

-- ============================================
-- 第一步：创建座位表
-- ============================================
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '所属自习室ID',
    `seat_number` VARCHAR(20) NOT NULL COMMENT '座位编号（如：A01, A02等）',
    `seat_name` VARCHAR(50) DEFAULT NULL COMMENT '座位名称',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-已被预约，2-被长期租赁',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_room_seat_number` (`room_id`, `seat_number`),
    CONSTRAINT `fk_seat_room` FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- ============================================
-- 第二步：为每个自习室创建座位
-- ============================================
-- A区30个座位
INSERT INTO `seat` (`room_id`, `seat_number`, `seat_name`, `status`) VALUES
(1, 'A01', 'A区-01号座位', 0), (1, 'A02', 'A区-02号座位', 0), (1, 'A03', 'A区-03号座位', 0),
(1, 'A04', 'A区-04号座位', 0), (1, 'A05', 'A区-05号座位', 0), (1, 'A06', 'A区-06号座位', 0),
(1, 'A07', 'A区-07号座位', 0), (1, 'A08', 'A区-08号座位', 0), (1, 'A09', 'A区-09号座位', 0),
(1, 'A10', 'A区-10号座位', 0), (1, 'A11', 'A区-11号座位', 0), (1, 'A12', 'A区-12号座位', 0),
(1, 'A13', 'A区-13号座位', 0), (1, 'A14', 'A区-14号座位', 0), (1, 'A15', 'A区-15号座位', 0),
(1, 'A16', 'A区-16号座位', 0), (1, 'A17', 'A区-17号座位', 0), (1, 'A18', 'A区-18号座位', 0),
(1, 'A19', 'A区-19号座位', 0), (1, 'A20', 'A区-20号座位', 0), (1, 'A21', 'A区-21号座位', 0),
(1, 'A22', 'A区-22号座位', 0), (1, 'A23', 'A区-23号座位', 0), (1, 'A24', 'A区-24号座位', 0),
(1, 'A25', 'A区-25号座位', 0), (1, 'A26', 'A区-26号座位', 0), (1, 'A27', 'A区-27号座位', 0),
(1, 'A28', 'A区-28号座位', 0), (1, 'A29', 'A区-29号座位', 0), (1, 'A30', 'A区-30号座位', 0);

-- B区25个座位
INSERT INTO `seat` (`room_id`, `seat_number`, `seat_name`, `status`) VALUES
(2, 'B01', 'B区-01号座位', 0), (2, 'B02', 'B区-02号座位', 0), (2, 'B03', 'B区-03号座位', 0),
(2, 'B04', 'B区-04号座位', 0), (2, 'B05', 'B区-05号座位', 0), (2, 'B06', 'B区-06号座位', 0),
(2, 'B07', 'B区-07号座位', 0), (2, 'B08', 'B区-08号座位', 0), (2, 'B09', 'B区-09号座位', 0),
(2, 'B10', 'B区-10号座位', 0), (2, 'B11', 'B区-11号座位', 0), (2, 'B12', 'B区-12号座位', 0),
(2, 'B13', 'B区-13号座位', 0), (2, 'B14', 'B区-14号座位', 0), (2, 'B15', 'B区-15号座位', 0),
(2, 'B16', 'B区-16号座位', 0), (2, 'B17', 'B区-17号座位', 0), (2, 'B18', 'B区-18号座位', 0),
(2, 'B19', 'B区-19号座位', 0), (2, 'B20', 'B区-20号座位', 0), (2, 'B21', 'B区-21号座位', 0),
(2, 'B22', 'B区-22号座位', 0), (2, 'B23', 'B区-23号座位', 0), (2, 'B24', 'B区-24号座位', 0),
(2, 'B25', 'B区-25号座位', 0);

-- C区20个座位
INSERT INTO `seat` (`room_id`, `seat_number`, `seat_name`, `status`) VALUES
(3, 'C01', 'C区-01号座位', 0), (3, 'C02', 'C区-02号座位', 0), (3, 'C03', 'C区-03号座位', 0),
(3, 'C04', 'C区-04号座位', 0), (3, 'C05', 'C区-05号座位', 0), (3, 'C06', 'C区-06号座位', 0),
(3, 'C07', 'C区-07号座位', 0), (3, 'C08', 'C区-08号座位', 0), (3, 'C09', 'C区-09号座位', 0),
(3, 'C10', 'C区-10号座位', 0), (3, 'C11', 'C区-11号座位', 0), (3, 'C12', 'C区-12号座位', 0),
(3, 'C13', 'C区-13号座位', 0), (3, 'C14', 'C区-14号座位', 0), (3, 'C15', 'C区-15号座位', 0),
(3, 'C16', 'C区-16号座位', 0), (3, 'C17', 'C区-17号座位', 0), (3, 'C18', 'C区-18号座位', 0),
(3, 'C19', 'C区-19号座位', 0), (3, 'C20', 'C区-20号座位', 0);

-- D区35个座位
INSERT INTO `seat` (`room_id`, `seat_number`, `seat_name`, `status`) VALUES
(4, 'D01', 'D区-01号座位', 0), (4, 'D02', 'D区-02号座位', 0), (4, 'D03', 'D区-03号座位', 0),
(4, 'D04', 'D区-04号座位', 0), (4, 'D05', 'D区-05号座位', 0), (4, 'D06', 'D区-06号座位', 0),
(4, 'D07', 'D区-07号座位', 0), (4, 'D08', 'D区-08号座位', 0), (4, 'D09', 'D区-09号座位', 0),
(4, 'D10', 'D区-10号座位', 0), (4, 'D11', 'D区-11号座位', 0), (4, 'D12', 'D区-12号座位', 0),
(4, 'D13', 'D区-13号座位', 0), (4, 'D14', 'D区-14号座位', 0), (4, 'D15', 'D区-15号座位', 0),
(4, 'D16', 'D区-16号座位', 0), (4, 'D17', 'D区-17号座位', 0), (4, 'D18', 'D区-18号座位', 0),
(4, 'D19', 'D区-19号座位', 0), (4, 'D20', 'D区-20号座位', 0), (4, 'D21', 'D区-21号座位', 0),
(4, 'D22', 'D区-22号座位', 0), (4, 'D23', 'D区-23号座位', 0), (4, 'D24', 'D区-24号座位', 0),
(4, 'D25', 'D区-25号座位', 0), (4, 'D26', 'D区-26号座位', 0), (4, 'D27', 'D区-27号座位', 0),
(4, 'D28', 'D区-28号座位', 0), (4, 'D29', 'D区-29号座位', 0), (4, 'D30', 'D区-30号座位', 0),
(4, 'D31', 'D区-31号座位', 0), (4, 'D32', 'D区-32号座位', 0), (4, 'D33', 'D区-33号座位', 0),
(4, 'D34', 'D区-34号座位', 0), (4, 'D35', 'D区-35号座位', 0);

-- E区28个座位
INSERT INTO `seat` (`room_id`, `seat_number`, `seat_name`, `status`) VALUES
(5, 'E01', 'E区-01号座位', 0), (5, 'E02', 'E区-02号座位', 0), (5, 'E03', 'E区-03号座位', 0),
(5, 'E04', 'E区-04号座位', 0), (5, 'E05', 'E区-05号座位', 0), (5, 'E06', 'E区-06号座位', 0),
(5, 'E07', 'E区-07号座位', 0), (5, 'E08', 'E区-08号座位', 0), (5, 'E09', 'E区-09号座位', 0),
(5, 'E10', 'E区-10号座位', 0), (5, 'E11', 'E区-11号座位', 0), (5, 'E12', 'E区-12号座位', 0),
(5, 'E13', 'E区-13号座位', 0), (5, 'E14', 'E区-14号座位', 0), (5, 'E15', 'E区-15号座位', 0),
(5, 'E16', 'E区-16号座位', 0), (5, 'E17', 'E区-17号座位', 0), (5, 'E18', 'E区-18号座位', 0),
(5, 'E19', 'E区-19号座位', 0), (5, 'E20', 'E区-20号座位', 0), (5, 'E21', 'E区-21号座位', 0),
(5, 'E22', 'E区-22号座位', 0), (5, 'E23', 'E区-23号座位', 0), (5, 'E24', 'E区-24号座位', 0),
(5, 'E25', 'E区-25号座位', 0), (5, 'E26', 'E区-26号座位', 0), (5, 'E27', 'E区-27号座位', 0),
(5, 'E28', 'E区-28号座位', 0);

-- ============================================
-- 第三步：为预约表添加 seat_id 字段
-- ============================================
-- 先添加字段（允许为空）
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

-- 将 seat_id 设置为 NOT NULL 并添加外键约束
ALTER TABLE `reservation` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_reservation_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- ============================================
-- 第四步：为长期租赁表添加 seat_id 字段
-- ============================================
-- 先添加字段（允许为空）
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

-- 将 seat_id 设置为 NOT NULL 并添加外键约束
ALTER TABLE `long_term_lease` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_lease_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);

-- ============================================
-- 第五步：为使用记录表添加 seat_id 字段
-- ============================================
-- 先添加字段（允许为空）
ALTER TABLE `usage_record` 
ADD COLUMN `seat_id` BIGINT(20) NULL COMMENT '座位ID' AFTER `room_id`;

-- 为现有使用记录分配座位
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

-- 将 seat_id 设置为 NOT NULL 并添加外键约束
ALTER TABLE `usage_record` 
MODIFY COLUMN `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
ADD KEY `idx_seat_id` (`seat_id`),
ADD CONSTRAINT `fk_record_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`);


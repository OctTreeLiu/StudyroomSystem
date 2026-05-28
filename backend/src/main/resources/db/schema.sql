-- 逐光自习室管理系统数据库脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS studyroom_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE studyroom_db;

-- 用户表（包含普通用户和管理员）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(64) NOT NULL COMMENT '密码（加密存储，MD5十六进制32位；预留至BCrypt）',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar_url` TEXT DEFAULT NULL COMMENT '头像URL（Base64或URL）',
    `role` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 自习室表（五个大区）
DROP TABLE IF EXISTS `study_room`;
CREATE TABLE `study_room` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自习室ID',
    `room_number` VARCHAR(20) NOT NULL COMMENT '自习室编号',
    `room_name` VARCHAR(100) NOT NULL COMMENT '自习室名称',
    `capacity` INT(11) NOT NULL DEFAULT 1 COMMENT '容量（座位数）',
    `location` VARCHAR(200) DEFAULT NULL COMMENT '位置描述',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-已被预约，2-被长期租赁，3-维护中',
    `description` TEXT COMMENT '自习室描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_number` (`room_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室表（五个大区）';

-- 座位表
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '所属自习室ID',
    `seat_number` VARCHAR(20) NOT NULL COMMENT '座位编号（如：A01, A02等）',
    `seat_name` VARCHAR(50) DEFAULT NULL COMMENT '座位名称',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-空闲，1-已被预约，2-被长期租赁，3-维护中，4-已锁定（未支付状态）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_room_seat_number` (`room_id`, `seat_number`),
    CONSTRAINT `fk_seat_room` FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- 预约表
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '自习室ID',
    `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
    `reservation_number` VARCHAR(50) NOT NULL COMMENT '预约编号',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待付款，1-已付款待使用，2-使用中，3-已完成，4-已取消',
    `payment_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '付款状态：0-未付款，1-已付款',
    `payment_time` DATETIME DEFAULT NULL COMMENT '付款时间',
    `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reservation_number` (`reservation_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_seat_id` (`seat_id`),
    KEY `idx_start_time` (`start_time`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_reservation_room` FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`),
    CONSTRAINT `fk_reservation_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 长期租赁表
DROP TABLE IF EXISTS `long_term_lease`;
CREATE TABLE `long_term_lease` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '租赁ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '自习室ID',
    `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
    `lease_number` VARCHAR(50) NOT NULL COMMENT '租赁编号',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-审核通过待付款，2-已付款生效，3-已拒绝，4-已过期',
    `payment_status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '付款状态：0-未付款，1-已付款',
    `payment_deadline` DATETIME DEFAULT NULL COMMENT '付款截止时间（审核通过后24小时内）',
    `payment_time` DATETIME DEFAULT NULL COMMENT '付款时间',
    `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '金额',
    `apply_reason` TEXT COMMENT '申请理由',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lease_number` (`lease_number`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_seat_id` (`seat_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_lease_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_lease_room` FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`),
    CONSTRAINT `fk_lease_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='长期租赁表';

-- 公告表
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `publisher_id` BIGINT(20) NOT NULL COMMENT '发布人ID（管理员）',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-发布中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_announcement_user` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 使用记录表
DROP TABLE IF EXISTS `usage_record`;
CREATE TABLE `usage_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '自习室ID',
    `seat_id` BIGINT(20) NOT NULL COMMENT '座位ID',
    `reservation_id` BIGINT(20) DEFAULT NULL COMMENT '预约ID（如果是预约使用）',
    `lease_id` BIGINT(20) DEFAULT NULL COMMENT '租赁ID（如果是租赁使用）',
    `start_time` DATETIME NOT NULL COMMENT '开始使用时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束使用时间',
    `duration` INT(11) DEFAULT NULL COMMENT '使用时长（分钟）',
    `type` TINYINT(1) NOT NULL COMMENT '使用类型：1-预约使用，2-长期租赁使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_seat_id` (`seat_id`),
    KEY `idx_start_time` (`start_time`),
    CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_record_room` FOREIGN KEY (`room_id`) REFERENCES `study_room` (`id`),
    CONSTRAINT `fk_record_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用记录表';

-- 插入初始管理员账号（密码：admin123，已使用MD5加密存储）
-- 注意：实际部署时密码应使用BCrypt等加密方式
-- admin123 的 MD5 值为：0192023a7bbd73250516f069df18b500
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) 
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 1, 1);

-- 插入示例自习室数据（五个大区）
INSERT INTO `study_room` (`room_number`, `room_name`, `capacity`, `location`, `status`, `description`) VALUES
('SR001', '逐光自习室A区', 30, '教学楼1楼101室', 0, '安静舒适的自习环境，配备空调和WiFi'),
('SR002', '逐光自习室B区', 25, '教学楼1楼102室', 0, '明亮宽敞的学习空间'),
('SR003', '逐光自习室C区', 20, '教学楼2楼201室', 0, '适合小组讨论和学习'),
('SR004', '逐光自习室D区', 35, '教学楼2楼202室', 0, '24小时开放，提供电源插座'),
('SR005', '逐光自习室E区', 28, '图书馆3楼301室', 0, '环境优雅，学习氛围浓厚');

-- 为每个自习室创建座位
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

-- 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID（NULL表示发送给所有管理员）',
    `type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '消息类型：1-预约提醒，2-系统通知',
    `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `related_id` BIGINT(20) DEFAULT NULL COMMENT '关联ID（如预约ID）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';


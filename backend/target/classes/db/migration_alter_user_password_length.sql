-- 将 user.password 从 VARCHAR(255) 收窄为 VARCHAR(64)
-- 说明：当前 PasswordUtil 使用 MD5 十六进制，固定 32 字符；64 可覆盖日后改为 BCrypt（约 60 字符）的常见长度。
-- 执行前确认：现有密码列中无超过 64 字符的值（MD5 场景下恒为 32，可安全执行）。

USE studyroom_db;

ALTER TABLE `user`
  MODIFY COLUMN `password` VARCHAR(64) NOT NULL COMMENT '密码（加密存储，MD5十六进制32位；预留至BCrypt）';

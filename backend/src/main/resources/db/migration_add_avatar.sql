-- 添加用户头像字段迁移脚本
USE studyroom_db;

-- 添加头像URL字段
ALTER TABLE `user` 
ADD COLUMN `avatar_url` TEXT DEFAULT NULL COMMENT '头像URL（Base64或URL）' AFTER `email`;

-- 如果字段已存在但类型不对，修改字段类型
ALTER TABLE `user` 
MODIFY COLUMN `avatar_url` TEXT DEFAULT NULL COMMENT '头像URL（Base64或URL）';


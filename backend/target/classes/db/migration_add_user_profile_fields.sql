-- 数据库迁移脚本：为用户表添加个人信息扩展字段
-- 执行此脚本前，请确保已经创建了 user 表

USE studyroom_db;

-- 添加性别字段
ALTER TABLE `user` 
ADD COLUMN `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别：男/女' AFTER `avatar_url`;

-- 添加年龄字段
ALTER TABLE `user` 
ADD COLUMN `age` INT DEFAULT NULL COMMENT '年龄' AFTER `gender`;

-- 添加兴趣爱好字段
ALTER TABLE `user` 
ADD COLUMN `hobby` VARCHAR(200) DEFAULT NULL COMMENT '兴趣爱好' AFTER `age`;

-- 添加在读/毕业高校字段
ALTER TABLE `user` 
ADD COLUMN `university` VARCHAR(100) DEFAULT NULL COMMENT '在读/毕业高校' AFTER `hobby`;

-- 添加个性签名字段（默认值在应用层设置）
ALTER TABLE `user` 
ADD COLUMN `signature` VARCHAR(200) DEFAULT NULL COMMENT '个性签名' AFTER `university`;


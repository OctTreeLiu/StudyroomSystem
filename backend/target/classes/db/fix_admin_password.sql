-- 修复管理员密码：将明文密码更新为MD5加密值
-- admin123 的 MD5 值为：0192023a7bbd73250516f069df18b500

USE studyroom_db;

-- 更新管理员密码为MD5加密值
UPDATE `user` 
SET `password` = '0192023a7bbd73250516f069df18b500' 
WHERE `username` = 'admin';

-- 验证更新结果
SELECT id, username, password, real_name, role, status 
FROM `user` 
WHERE `username` = 'admin';


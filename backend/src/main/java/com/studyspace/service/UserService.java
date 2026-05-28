package com.studyspace.service;

import com.studyspace.entity.User;
import com.studyspace.mapper.UserMapper;
import com.studyspace.utils.PasswordUtil;
import com.studyspace.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 用户注册
     */
    public User register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(0); // 默认普通用户
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        
        // 插入用户
        userMapper.insert(user);
        return user;
    }
    
    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        return user;
    }
    
    /**
     * 根据ID查询用户
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 根据用户名查询用户
     */
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    /**
     * 更新用户信息
     */
    public void updateUser(User user) {
        // 数据校验
        if (user.getGender() != null && !user.getGender().isEmpty()) {
            if (!user.getGender().equals("男") && !user.getGender().equals("女")) {
                throw new RuntimeException("性别只能为'男'或'女'");
            }
        }
        
        if (user.getAge() != null) {
            if (user.getAge() < 1 || user.getAge() > 150) {
                throw new RuntimeException("年龄必须在1-150之间");
            }
        }
        
        if (user.getHobby() != null && user.getHobby().length() > 200) {
            throw new RuntimeException("兴趣爱好长度不能超过200个字符");
        }
        
        if (user.getUniversity() != null && user.getUniversity().length() > 100) {
            throw new RuntimeException("高校名称长度不能超过100个字符");
        }
        
        if (user.getSignature() != null && user.getSignature().length() > 200) {
            throw new RuntimeException("个性签名长度不能超过200个字符");
        }
        
        // 会员类型校验
        if (user.getMemberType() != null) {
            if (user.getMemberType() < 0 || user.getMemberType() > 2) {
                throw new RuntimeException("会员类型只能为0（普通用户）、1（VIP）或2（SVIP）");
            }
            
            // 获取修改前的用户信息，用于判断是否从会员变为普通用户
            User existingUser = userMapper.selectById(user.getId());
            if (existingUser != null) {
                Integer beforeMemberType = existingUser.getMemberType() != null ? existingUser.getMemberType() : 0;
                Integer afterMemberType = user.getMemberType();
                
                // 如果从会员（VIP/SVIP）变为普通用户，将到期时间设置为当前时间
                if (beforeMemberType > 0 && afterMemberType == 0) {
                    user.setMemberExpireTime(java.time.LocalDateTime.now());
                } else if (afterMemberType == 0) {
                    // 如果直接设置为普通用户（且之前可能也是普通用户），清空会员到期时间
                    user.setMemberExpireTime(null);
                } else {
                    // 如果是会员，但没有设置到期时间，则设置为当前时间（表示已过期）
                    if (user.getMemberExpireTime() == null) {
                        user.setMemberExpireTime(java.time.LocalDateTime.now());
                    }
                }
            } else {
                // 如果用户不存在，按原逻辑处理
                if (user.getMemberType() == 0) {
                    user.setMemberExpireTime(null);
                } else {
                    if (user.getMemberExpireTime() == null) {
                        user.setMemberExpireTime(java.time.LocalDateTime.now());
                    }
                }
            }
        }
        
        userMapper.update(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new RuntimeException("旧密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!PasswordUtil.verify(oldPassword, existingUser.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        User user = new User();
        user.setId(userId);
        user.setPassword(PasswordUtil.encrypt(newPassword));
        userMapper.update(user);
    }
    
    /**
     * 转换为VO对象
     */
    public UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        
        // 设置个性签名默认值
        if (vo.getSignature() == null || vo.getSignature().trim().isEmpty()) {
            vo.setSignature("是一条有梦想的咸鱼......");
        }
        
        return vo;
    }
    
    /**
     * 获取UserMapper（供Controller使用）
     */
    public UserMapper getUserMapper() {
        return userMapper;
    }
}


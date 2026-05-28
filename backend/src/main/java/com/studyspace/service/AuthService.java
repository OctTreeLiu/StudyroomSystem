package com.studyspace.service;

import com.studyspace.dto.LoginDTO;
import com.studyspace.dto.RegisterDTO;
import com.studyspace.entity.User;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类
 */
@Service
public class AuthService {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户注册
     */
    public Map<String, Object> register(RegisterDTO registerDTO) {
        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 创建用户对象
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 注册用户
        User savedUser = userService.register(user);
        
        // 生成token
        String token = JwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userService.convertToVO(savedUser));
        
        return result;
    }
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 登录验证
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        
        // 生成token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userService.convertToVO(user));
        
        return result;
    }
}


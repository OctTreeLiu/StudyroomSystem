package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.dto.LoginDTO;
import com.studyspace.dto.RegisterDTO;
import com.studyspace.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@ComponentScan
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Validated @RequestBody RegisterDTO registerDTO) {
        try {
            Map<String, Object> result = authService.register(registerDTO);
            return success("注册成功", result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = authService.login(loginDTO);
            return success("登录成功", result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }
}


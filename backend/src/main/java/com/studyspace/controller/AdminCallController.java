package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.AdminCall;
import com.studyspace.service.AdminCallService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员呼叫控制器（用户接口）
 */
@RestController
@RequestMapping("/admin-call")
public class AdminCallController extends BaseController {
    
    @Autowired
    private AdminCallService adminCallService;
    
    /**
     * 呼叫管理员（用户接口）
     */
    @PostMapping("/call")
    public Result<AdminCall> callAdmin(@RequestHeader("Authorization") String token,
                                       @RequestBody Map<String, String> request) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            String message = request.get("message");
            
            AdminCall adminCall = adminCallService.createCall(userId, message);
            return success("呼叫成功，管理员将尽快联系您", adminCall);
        } catch (Exception e) {
            return error("呼叫失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取我的呼叫记录（用户接口）
     */
    @GetMapping("/my-calls")
    public Result<List<AdminCall>> getMyCalls(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            List<AdminCall> calls = adminCallService.getCallsByUserId(userId);
            return success(calls);
        } catch (Exception e) {
            return error("获取呼叫记录失败：" + e.getMessage());
        }
    }
}


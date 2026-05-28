package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.AdminCall;
import com.studyspace.service.AdminCallService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员呼叫管理控制器
 */
@RestController
@RequestMapping("/admin/call")
public class AdminCallManagementController extends BaseController {
    
    @Autowired
    private AdminCallService adminCallService;
    
    /**
     * 查询所有呼叫记录（管理员）- 支持分页
     */
    @GetMapping("/list")
    public Result<PageResult<AdminCall>> getAllCalls(@RequestHeader("Authorization") String token,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }

            PageResult<AdminCall> pageResult = adminCallService.getAllCallsWithPagination(page, pageSize, status, keyword);
            
            return success(pageResult);
        } catch (Exception e) {
            return error("查询呼叫记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 处理呼叫（标记为已处理）
     */
    @PutMapping("/handle/{id}")
    public Result<AdminCall> handleCall(@RequestHeader("Authorization") String token,
                                         @PathVariable Long id) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            Long adminId = JwtUtil.getUserIdFromToken(actualToken);
            adminCallService.handleCall(id, adminId);
            
            AdminCall adminCall = adminCallService.getAllCalls().stream()
                .filter(call -> call.getId().equals(id))
                .findFirst()
                .orElse(null);
            
            return success("处理成功", adminCall);
        } catch (Exception e) {
            return error("处理失败：" + e.getMessage());
        }
    }
    
    /**
     * 统计待处理数量
     */
    @GetMapping("/pending-count")
    public Result<Map<String, Object>> getPendingCount(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            String actualToken = token.replace("Bearer ", "").trim();
            if (actualToken.isEmpty()) {
                return error(401, "未授权，请先登录");
            }
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            int count = adminCallService.countPending();
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return success(result);
        } catch (Exception e) {
            return error("获取待处理数量失败：" + e.getMessage());
        }
    }
}


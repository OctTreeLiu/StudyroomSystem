package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.Notification;
import com.studyspace.service.NotificationService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/notification")
public class NotificationController extends BaseController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取当前用户的消息列表
     */
    @GetMapping("/list")
    public Result<List<Notification>> getMyNotifications(@RequestHeader("Authorization") String token) {
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
            
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            return success(notifications);
        } catch (Exception e) {
            return error("获取消息列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户的未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(@RequestHeader("Authorization") String token) {
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
            
            int unreadCount = notificationService.getUnreadCountByUserId(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("unreadCount", unreadCount);
            return success(result);
        } catch (Exception e) {
            return error("获取未读消息数量失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@RequestHeader("Authorization") String token,
                                   @PathVariable Long id) {
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
            
            notificationService.markAsRead(id);
            return success("消息已标记为已读", null);
        } catch (Exception e) {
            return error("标记消息失败：" + e.getMessage());
        }
    }
    
    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
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
            
            notificationService.markAllAsRead(userId);
            return success("所有消息已标记为已读", null);
        } catch (Exception e) {
            return error("标记消息失败：" + e.getMessage());
        }
    }
}


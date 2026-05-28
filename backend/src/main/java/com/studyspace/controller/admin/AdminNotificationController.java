package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.Notification;
import com.studyspace.service.NotificationService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员消息通知控制器
 */
@RestController
@RequestMapping("/admin/notification")
public class AdminNotificationController extends BaseController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取管理员的消息列表（包括发送给所有管理员的消息）
     */
    @GetMapping("/list")
    public Result<List<Notification>> getAdminNotifications(@RequestHeader("Authorization") String token) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            // 获取发送给所有管理员的消息（userId为NULL）
            List<Notification> adminNotifications = notificationService.getAdminNotifications();
            
            // 如果管理员也有个人消息，可以合并
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId != null) {
                List<Notification> userNotifications = notificationService.getNotificationsByUserId(userId);
                adminNotifications.addAll(userNotifications);
            }
            
            // 按创建时间倒序排序
            adminNotifications.sort((a, b) -> {
                if (a.getCreateTime() == null || b.getCreateTime() == null) {
                    return 0;
                }
                return b.getCreateTime().compareTo(a.getCreateTime());
            });
            
            return success(adminNotifications);
        } catch (Exception e) {
            return error("获取消息列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取管理员的未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getAdminUnreadCount(@RequestHeader("Authorization") String token) {
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
                return error(403, "无权限访问，仅管理员可查看");
            }
            
            // 统计管理员消息（userId为NULL）的未读数量
            List<Notification> adminNotifications = notificationService.getAdminNotifications();
            long unreadCount = adminNotifications.stream()
                .filter(n -> n.getIsRead() == null || n.getIsRead() == 0)
                .count();
            
            // 加上个人消息的未读数量
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId != null) {
                unreadCount += notificationService.getUnreadCountByUserId(userId);
            }
            
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
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
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
            
            Integer role = JwtUtil.getRoleFromToken(actualToken);
            if (role == null) {
                return error(401, "无效的token，请重新登录");
            }
            
            if (role != 1) {
                return error(403, "无权限访问，仅管理员可操作");
            }
            
            // 标记管理员消息为已读（需要单独处理userId为NULL的消息）
            List<Notification> adminNotifications = notificationService.getAdminNotifications();
            for (Notification notification : adminNotifications) {
                if (notification.getIsRead() == null || notification.getIsRead() == 0) {
                    notificationService.markAsRead(notification.getId());
                }
            }
            
            // 标记个人消息为已读
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId != null) {
                notificationService.markAllAsRead(userId);
            }
            
            return success("所有消息已标记为已读", null);
        } catch (Exception e) {
            return error("标记消息失败：" + e.getMessage());
        }
    }
    
    /**
     * 管理员向单个用户发送通知
     */
    @PostMapping("/send-to-user")
    public Result<Void> sendNotificationToUser(@RequestHeader("Authorization") String token,
                                                 @RequestBody Map<String, Object> request) {
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
            
            Object userIdObj = request.get("userId");
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            
            if (userIdObj == null) {
                return error("用户ID不能为空");
            }
            Long userId;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else {
                return error("用户ID格式不正确");
            }
            
            if (title == null || title.trim().isEmpty()) {
                return error("通知标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return error("通知内容不能为空");
            }
            
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(3); // 管理员通知
            notification.setTitle(title);
            notification.setContent(content);
            notification.setIsRead(0);
            
            notificationService.createNotification(notification);
            
            return success("通知发送成功", null);
        } catch (Exception e) {
            return error("发送通知失败：" + e.getMessage());
        }
    }
}


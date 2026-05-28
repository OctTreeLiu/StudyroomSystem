package com.studyspace.controller.admin;

import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.Notification;
import com.studyspace.entity.User;
import com.studyspace.service.NotificationService;
import com.studyspace.service.UserService;
import com.studyspace.service.UserModifyLogService;
import com.studyspace.utils.JwtUtil;
import com.studyspace.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户管理控制器
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserModifyLogService userModifyLogService;
    
    /**
     * 查询所有用户（管理员使用）
     */
    @GetMapping("/list")
    public Result<List<UserVO>> getAllUsers(@RequestHeader("Authorization") String token) {
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
            
            List<User> users = userService.getUserMapper().selectAll();
            List<UserVO> userVOs = users.stream()
                .map(userService::convertToVO)
                .collect(Collectors.toList());
            return success(userVOs);
        } catch (Exception e) {
            return error("查询用户列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return error("用户不存在");
            }
            UserVO userVO = userService.convertToVO(user);
            return success(userVO);
        } catch (Exception e) {
            return error("查询用户信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息（管理员）
     */
    @PutMapping("/update/{id}")
    public Result<UserVO> updateUser(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id,
                                     @RequestBody User user) {
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
            
            user.setId(id);
            // 管理员不能修改密码和角色
            user.setPassword(null);
            user.setRole(null);
            
            // 获取修改前的用户信息（用于记录日志）
            User beforeUser = userService.getUserById(id);
            if (beforeUser == null) {
                return error("用户不存在");
            }
            
            // 获取管理员信息
            Long adminId = JwtUtil.getUserIdFromToken(actualToken);
            String adminUsername = JwtUtil.getUsernameFromToken(actualToken);
            if (adminId == null || adminUsername == null) {
                return error("无法获取管理员信息");
            }
            
            userService.updateUser(user);
            User updatedUser = userService.getUserById(id);
            UserVO userVO = userService.convertToVO(updatedUser);
            
            // 记录修改日志（当修改会员身份时）
            try {
                userModifyLogService.logMemberTypeChange(id, beforeUser, updatedUser, adminId, adminUsername);
            } catch (Exception e) {
                // 日志记录失败不影响更新操作，只记录错误
                System.err.println("记录用户修改日志失败: " + e.getMessage());
            }
            
            // 发送通知给被修改的用户
            try {
                Notification notification = new Notification();
                notification.setUserId(id); // 发送给被修改的用户
                notification.setType(2); // 系统通知
                notification.setTitle("信息变更通知");
                notification.setContent("管理员对您的信息进行了更改！");
                notification.setIsRead(0);
                notificationService.createNotification(notification);
            } catch (Exception e) {
                // 通知发送失败不影响更新操作，只记录日志
                System.err.println("发送用户信息变更通知失败: " + e.getMessage());
            }
            
            return success("更新成功", userVO);
        } catch (Exception e) {
            return error("更新用户信息失败：" + e.getMessage());
        }
    }
}


package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.entity.User;
import com.studyspace.service.UserService;
import com.studyspace.utils.JwtUtil;
import com.studyspace.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 从token中获取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return error("用户不存在");
            }
            
            UserVO userVO = userService.convertToVO(user);
            return success(userVO);
        } catch (Exception e) {
            return error("获取用户信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserVO> updateUserInfo(@RequestHeader("Authorization") String token,
                                         @RequestBody User user) {
        try {
            // 从token中获取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            // 设置用户ID，防止修改其他用户信息
            user.setId(userId);
            // 不允许修改密码、角色等敏感字段
            user.setPassword(null);
            user.setRole(null);
            user.setStatus(null);
            
            userService.updateUser(user);
            User updatedUser = userService.getUserById(userId);
            UserVO userVO = userService.convertToVO(updatedUser);
            
            return success("更新成功", userVO);
        } catch (Exception e) {
            return error("更新用户信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传用户头像
     */
    @PutMapping("/avatar")
    public Result<UserVO> uploadAvatar(@RequestHeader("Authorization") String token,
                                       @RequestBody java.util.Map<String, String> request) {
        try {
            // 从token中获取用户ID
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            String avatarUrl = request.get("avatarUrl");
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                return error("头像数据不能为空");
            }
            
            // 验证base64格式（简单验证）
            if (!avatarUrl.startsWith("data:image/")) {
                return error("头像格式不正确，请使用图片文件");
            }
            
            // 限制头像大小：1MB图片base64编码后约为1.4MB（1MB = 1048576字节，base64编码增加约33%）
            // 设置为1400000字节，确保实际图片不超过1MB
            if (avatarUrl.length() > 1400000) {
                return error("头像文件过大，上传的头像大小不得超过1M");
            }
            
            User user = new User();
            user.setId(userId);
            user.setAvatarUrl(avatarUrl);
            
            userService.updateUser(user);
            User updatedUser = userService.getUserById(userId);
            UserVO userVO = userService.convertToVO(updatedUser);
            
            return success("头像上传成功", userVO);
        } catch (Exception e) {
            return error("上传头像失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     * 流程：校验旧密码 -> 输入新密码 -> 确认新密码（前端校验） -> 修改成功
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token,
                                       @RequestBody Map<String, String> request) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                return error("无效的token");
            }

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            userService.changePassword(userId, oldPassword, newPassword);

            return success("修改成功", null);
        } catch (Exception e) {
            return error("修改密码失败：" + e.getMessage());
        }
    }
}


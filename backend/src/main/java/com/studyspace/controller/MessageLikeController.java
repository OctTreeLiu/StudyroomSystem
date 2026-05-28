package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.service.MessageLikeService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 留言点赞控制器
 */
@RestController
@RequestMapping("/message-like")
public class MessageLikeController extends BaseController {
    
    @Autowired
    private MessageLikeService messageLikeService;
    
    /**
     * 点赞或取消点赞
     */
    @PostMapping("/toggle/{messageId}")
    public Result<Map<String, Object>> toggleLike(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long messageId) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            boolean isLiked = messageLikeService.toggleLike(messageId, userId);
            int likeCount = messageLikeService.countLikesByMessageId(messageId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("isLiked", isLiked);
            result.put("likeCount", likeCount);
            
            return success(isLiked ? "点赞成功" : "取消点赞成功", result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("操作失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查是否已点赞
     */
    @GetMapping("/check/{messageId}")
    public Result<Boolean> checkLike(@RequestHeader("Authorization") String token,
                                     @PathVariable Long messageId) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            boolean isLiked = messageLikeService.isLiked(messageId, userId);
            return success(isLiked);
        } catch (Exception e) {
            return error("查询失败：" + e.getMessage());
        }
    }
}


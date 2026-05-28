package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.service.CommentLikeService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论点赞控制器
 */
@RestController
@RequestMapping("/comment-like")
public class CommentLikeController extends BaseController {
    
    @Autowired
    private CommentLikeService commentLikeService;
    
    /**
     * 点赞或取消点赞
     */
    @PostMapping("/toggle/{commentId}")
    public Result<Map<String, Object>> toggleLike(@RequestHeader("Authorization") String token,
                                                  @PathVariable Long commentId) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            boolean isLiked = commentLikeService.toggleLike(commentId, userId);
            int likeCount = commentLikeService.countLikesByCommentId(commentId);
            
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
    @GetMapping("/check/{commentId}")
    public Result<Boolean> checkLike(@RequestHeader("Authorization") String token,
                                     @PathVariable Long commentId) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            boolean isLiked = commentLikeService.isLiked(commentId, userId);
            return success(isLiked);
        } catch (Exception e) {
            return error("查询失败：" + e.getMessage());
        }
    }
}


package com.studyspace.controller;

import com.studyspace.common.Result;
import com.studyspace.dto.MessageCommentDTO;
import com.studyspace.entity.MessageComment;
import com.studyspace.service.MessageCommentService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言评论控制器
 */
@RestController
@RequestMapping("/message-comment")
public class MessageCommentController extends BaseController {
    
    @Autowired
    private MessageCommentService messageCommentService;
    
    /**
     * 根据留言ID查询评论列表
     */
    @GetMapping("/list/{messageId}")
    public Result<List<MessageComment>> getCommentList(@PathVariable Long messageId,
                                                       @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentUserId = null;
            if (token != null && !token.trim().isEmpty()) {
                try {
                    String actualToken = token.replace("Bearer ", "");
                    currentUserId = JwtUtil.getUserIdFromToken(actualToken);
                } catch (Exception e) {
                    // token无效，忽略，继续查询但不设置点赞状态
                }
            }
            List<MessageComment> comments = messageCommentService.getCommentsByMessageId(messageId, currentUserId);
            return success(comments);
        } catch (Exception e) {
            return error("查询评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建评论
     */
    @PostMapping("/create")
    public Result<MessageComment> createComment(@RequestHeader("Authorization") String token,
                                                @Validated @RequestBody MessageCommentDTO commentDTO) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            MessageComment comment = messageCommentService.createComment(
                userId,
                commentDTO.getMessageId(),
                commentDTO.getParentId(),
                commentDTO.getContent(),
                commentDTO.getIsAnonymous()
            );
            
            return success("评论发布成功", comment);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("发布评论失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户删除自己的评论
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteComment(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            messageCommentService.deleteCommentByUser(id, userId);
            return success("删除评论成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除评论失败：" + e.getMessage());
        }
    }
}


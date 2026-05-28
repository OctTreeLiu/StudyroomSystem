package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.MessageComment;
import com.studyspace.service.MessageCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员评论管理控制器
 */
@RestController
@RequestMapping("/admin/message-comment")
public class AdminMessageCommentController extends BaseController {
    
    @Autowired
    private MessageCommentService messageCommentService;
    
    /**
     * 分页查询所有评论列表（包含一级评论和次级评论）
     */
    @GetMapping("/list")
    public Result<PageResult<MessageComment>> getCommentList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username) {
        try {
            PageResult<MessageComment> pageResult = messageCommentService.getAllCommentsWithPagination(page, pageSize, username);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询评论列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除评论（管理员可删除任何评论，包括次级评论）
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        try {
            messageCommentService.deleteComment(id);
            return success("删除评论成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除评论失败：" + e.getMessage());
        }
    }
}


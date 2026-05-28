package com.studyspace.controller.admin;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.controller.BaseController;
import com.studyspace.entity.MessageBoard;
import com.studyspace.service.MessageBoardService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员留言板管理控制器
 */
@RestController
@RequestMapping("/admin/message-board")
public class AdminMessageBoardController extends BaseController {
    
    @Autowired
    private MessageBoardService messageBoardService;
    
    /**
     * 分页查询所有留言列表
     */
    @GetMapping("/list")
    public Result<PageResult<MessageBoard>> getMessageList(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "content", required = false) String content) {
        try {
            Long currentUserId = null;
            if (token != null && !token.trim().isEmpty()) {
                try {
                    String actualToken = token.replace("Bearer ", "");
                    currentUserId = JwtUtil.getUserIdFromToken(actualToken);
                } catch (Exception e) {
                    // token无效，忽略
                }
            }
            PageResult<MessageBoard> pageResult = messageBoardService.getAllMessagesWithPagination(page, pageSize, username, content, currentUserId);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询留言列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除留言
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        try {
            messageBoardService.deleteMessage(id);
            return success("删除留言成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除留言失败：" + e.getMessage());
        }
    }
}


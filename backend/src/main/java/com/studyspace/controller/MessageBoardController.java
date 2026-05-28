package com.studyspace.controller;

import com.studyspace.common.PageResult;
import com.studyspace.common.Result;
import com.studyspace.dto.MessageBoardDTO;
import com.studyspace.entity.MessageBoard;
import com.studyspace.service.MessageBoardService;
import com.studyspace.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 留言板控制器
 */
@RestController
@RequestMapping("/message-board")
public class MessageBoardController extends BaseController {
    
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
                    // token无效，忽略，继续查询但不设置点赞状态
                }
            }
            PageResult<MessageBoard> pageResult = messageBoardService.getAllMessagesWithPagination(page, pageSize, username, content, currentUserId);
            return success(pageResult);
        } catch (Exception e) {
            return error("查询留言列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布留言
     */
    @PostMapping("/create")
    public Result<MessageBoard> createMessage(@RequestHeader("Authorization") String token,
                                              @Validated @RequestBody MessageBoardDTO messageBoardDTO) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            MessageBoard message = messageBoardService.createMessage(
                userId, 
                messageBoardDTO.getContent(), 
                messageBoardDTO.getIsAnonymous()
            );
            
            return success("留言发布成功", message);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("发布留言失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询留言详情
     */
    @GetMapping("/{id}")
    public Result<MessageBoard> getMessageById(@PathVariable Long id) {
        try {
            MessageBoard message = messageBoardService.getMessageById(id);
            if (message == null) {
                return error("留言不存在");
            }
            return success(message);
        } catch (Exception e) {
            return error("查询留言详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 用户删除自己的留言
     */
    @PostMapping("/delete/{id}")
    public Result<Void> deleteMessage(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(actualToken);
            
            if (userId == null) {
                return error("无效的token");
            }
            
            messageBoardService.deleteMessageByUser(id, userId);
            return success("删除留言成功", null);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除留言失败：" + e.getMessage());
        }
    }
}


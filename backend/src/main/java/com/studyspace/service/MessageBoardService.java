package com.studyspace.service;

import com.studyspace.common.PageResult;
import com.studyspace.entity.MessageBoard;
import com.studyspace.mapper.MessageBoardMapper;
import com.studyspace.mapper.MessageLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 留言板服务类
 */
@Service
public class MessageBoardService {
    
    @Autowired
    private MessageBoardMapper messageBoardMapper;
    
    @Autowired
    private MessageLikeMapper messageLikeMapper;
    
    /**
     * 查询所有留言列表（按时间倒序，最新在前）
     */
    public List<MessageBoard> getAllMessages() {
        return messageBoardMapper.selectAll();
    }
    
    /**
     * 分页查询所有留言列表（按时间倒序，最新在前）
     * @param page 页码
     * @param pageSize 每页数量
     * @param username 用户名（可选，用于搜索）
     * @param content 留言内容（可选，用于模糊搜索）
     * @param currentUserId 当前用户ID（可选，用于判断是否已点赞）
     */
    public PageResult<MessageBoard> getAllMessagesWithPagination(Integer page, Integer pageSize, String username, String content, Long currentUserId) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        int offset = (page - 1) * pageSize;
        List<MessageBoard> list;
        long total;
        
        boolean hasUsername = username != null && !username.trim().isEmpty();
        boolean hasContent = content != null && !content.trim().isEmpty();
        
        if (hasUsername && hasContent) {
            // 同时按用户名和内容搜索
            list = messageBoardMapper.selectAllWithPaginationAndSearch(offset, pageSize, username.trim(), content.trim());
            total = messageBoardMapper.countAllBySearch(username.trim(), content.trim());
        } else if (hasUsername) {
            // 只按用户名搜索
            list = messageBoardMapper.selectAllWithPaginationAndUsername(offset, pageSize, username.trim());
            total = messageBoardMapper.countAllByUsername(username.trim());
        } else if (hasContent) {
            // 只按内容搜索
            list = messageBoardMapper.selectAllWithPaginationAndContent(offset, pageSize, content.trim());
            total = messageBoardMapper.countAllByContent(content.trim());
        } else {
            // 普通查询
            list = messageBoardMapper.selectAllWithPagination(offset, pageSize);
            total = messageBoardMapper.countAll();
        }
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            for (MessageBoard message : list) {
                boolean isLiked = messageLikeMapper.selectByMessageIdAndUserId(message.getId(), currentUserId) != null;
                message.setIsLiked(isLiked);
            }
        }
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 根据用户ID查询留言列表
     */
    public List<MessageBoard> getMessagesByUserId(Long userId) {
        return messageBoardMapper.selectByUserId(userId);
    }
    
    /**
     * 根据ID查询留言
     */
    public MessageBoard getMessageById(Long id) {
        return messageBoardMapper.selectById(id);
    }
    
    /**
     * 发布留言
     */
    @Transactional
    public MessageBoard createMessage(Long userId, String content, Boolean isAnonymous) {
        MessageBoard message = new MessageBoard();
        message.setUserId(userId);
        message.setContent(content);
        message.setIsAnonymous(isAnonymous != null && isAnonymous ? 1 : 0);
        
        messageBoardMapper.insert(message);
        
        // 返回包含用户名的完整信息（包含评论数和点赞数）
        MessageBoard result = messageBoardMapper.selectById(message.getId());
        if (result != null) {
            result.setCommentCount(0);
            result.setLikeCount(0);
            result.setIsLiked(false);
        }
        return result;
    }
    
    /**
     * 删除留言（管理员使用）
     */
    @Transactional
    public void deleteMessage(Long id) {
        MessageBoard message = messageBoardMapper.selectById(id);
        if (message == null) {
            throw new RuntimeException("留言不存在");
        }
        
        messageBoardMapper.deleteById(id);
    }
    
    /**
     * 用户删除自己的留言
     */
    @Transactional
    public void deleteMessageByUser(Long id, Long userId) {
        MessageBoard message = messageBoardMapper.selectById(id);
        if (message == null) {
            throw new RuntimeException("留言不存在");
        }
        
        // 验证是否为留言所有者
        if (!message.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的留言");
        }
        
        messageBoardMapper.deleteById(id);
    }
}

